/** @jest-environment jsdom */
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import React from 'react';
import { testHarness } from '../../testing/renderWithProviders';
import { AgendaScreen, parseDateTime } from './AgendaScreen';

const eventJson =
  '{"id":"1","title":"Reuniao","description":"pauta","startsAt":"2026-07-01T10:00:00Z",' +
  '"endsAt":null,"createdAt":"2026-06-01T00:00:00Z"}';

describe('parseDateTime', () => {
  it('converte data local para ISO e rejeita formato invalido', () => {
    expect(parseDateTime('2026-06-15 14:30')).toMatch(/^\d{4}-\d{2}-\d{2}T.*Z$/);
    expect(parseDateTime('15/06/2026')).toBeNull();
    expect(parseDateTime('')).toBeNull();
  });
});

describe('AgendaScreen', () => {
  it('lista eventos vindos da API', async () => {
    const harness = testHarness([{ body: `[${eventJson}]` }]);
    render(<AgendaScreen />, { wrapper: harness.wrapper });

    await waitFor(() => expect(screen.getByText('Reuniao')).toBeTruthy());
    expect(screen.getByText('pauta')).toBeTruthy();
  });

  it('mostra estado vazio sem eventos', async () => {
    const harness = testHarness([{ body: '[]' }]);
    render(<AgendaScreen />, { wrapper: harness.wrapper });

    await waitFor(() => expect(screen.getByText('Sem eventos')).toBeTruthy());
  });

  it('cria evento e recarrega a lista', async () => {
    const harness = testHarness([
      { body: '[]' },
      { status: 201, body: eventJson },
      { body: `[${eventJson}]` },
    ]);
    render(<AgendaScreen />, { wrapper: harness.wrapper });
    await waitFor(() => expect(screen.getByText('Sem eventos')).toBeTruthy());

    fireEvent.change(screen.getByTestId('agenda-title'), { target: { value: 'Reuniao' } });
    fireEvent.change(screen.getByTestId('agenda-starts-at'), {
      target: { value: '2026-07-01 10:00' },
    });
    fireEvent.click(screen.getByTestId('agenda-create'));

    await waitFor(() => expect(screen.getByText('Reuniao')).toBeTruthy());
    expect(harness.calls[1].init.method).toBe('POST');
  });

  it('valida data invalida sem chamar a API', async () => {
    const harness = testHarness([{ body: '[]' }]);
    render(<AgendaScreen />, { wrapper: harness.wrapper });
    await waitFor(() => expect(screen.getByText('Sem eventos')).toBeTruthy());

    fireEvent.change(screen.getByTestId('agenda-title'), { target: { value: 'X' } });
    fireEvent.change(screen.getByTestId('agenda-starts-at'), { target: { value: 'amanha' } });
    fireEvent.click(screen.getByTestId('agenda-create'));

    expect(screen.getByText('Data inválida — use AAAA-MM-DD HH:mm')).toBeTruthy();
    expect(harness.calls).toHaveLength(1);
  });
});
