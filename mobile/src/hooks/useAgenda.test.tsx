/** @jest-environment jsdom */
import { renderHook, waitFor } from '@testing-library/react';
import { testHarness } from '../testing/renderWithProviders';
import { useAgendaEvents, useCreateAgendaEvent } from './useAgenda';

const eventJson =
  '{"id":"1","title":"Reuniao","description":null,"startsAt":"2026-07-01T10:00:00Z",' +
  '"endsAt":null,"createdAt":"2026-06-01T00:00:00Z"}';

describe('useAgenda', () => {
  it('carrega os eventos', async () => {
    const harness = testHarness([{ body: `[${eventJson}]` }]);
    const { result } = renderHook(() => useAgendaEvents(), { wrapper: harness.wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data?.[0].title).toBe('Reuniao');
  });

  it('criar evento invalida e recarrega a lista', async () => {
    const harness = testHarness([
      { body: '[]' },
      { status: 201, body: eventJson },
      { body: `[${eventJson}]` },
    ]);
    const { result } = renderHook(
      () => ({ events: useAgendaEvents(), create: useCreateAgendaEvent() }),
      { wrapper: harness.wrapper },
    );
    await waitFor(() => expect(result.current.events.isSuccess).toBe(true));
    expect(result.current.events.data).toHaveLength(0);

    result.current.create.mutate({ title: 'Reuniao', startsAt: '2026-07-01T10:00:00Z' });

    await waitFor(() => expect(result.current.events.data).toHaveLength(1));
    expect(harness.calls).toHaveLength(3);
  });
});
