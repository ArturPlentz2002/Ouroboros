/** @jest-environment jsdom */
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import React from 'react';
import { testHarness } from '../../testing/renderWithProviders';
import { NotesScreen } from './NotesScreen';

const noteJson =
  '{"id":"n1","title":"Mercado","content":"comprar cafe","tags":["casa","compras"],' +
  '"createdAt":"2026-06-01T00:00:00Z","updatedAt":"2026-06-01T00:00:00Z"}';

describe('NotesScreen', () => {
  it('lista notas com tags', async () => {
    const harness = testHarness([{ body: `[${noteJson}]` }]);
    render(<NotesScreen />, { wrapper: harness.wrapper });

    await waitFor(() => expect(screen.getByText('Mercado')).toBeTruthy());
    expect(screen.getByText('casa')).toBeTruthy();
    expect(screen.getByText('compras')).toBeTruthy();
  });

  it('busca por texto envia o parametro q', async () => {
    const harness = testHarness([{ body: `[${noteJson}]` }, { body: '[]' }]);
    render(<NotesScreen />, { wrapper: harness.wrapper });
    await waitFor(() => expect(screen.getByText('Mercado')).toBeTruthy());

    fireEvent.change(screen.getByTestId('notes-search'), { target: { value: 'cafe' } });

    await waitFor(() => {
      const urls = harness.calls.map((c) => c.url);
      expect(urls).toContain('http://gw/api/v1/notes?q=cafe');
    });
  });

  it('cria nota com tags separadas por virgula', async () => {
    const harness = testHarness([
      { body: '[]' },
      { status: 201, body: noteJson },
      { body: `[${noteJson}]` },
    ]);
    render(<NotesScreen />, { wrapper: harness.wrapper });
    await waitFor(() => expect(screen.getByText('Sem notas')).toBeTruthy());

    fireEvent.change(screen.getByTestId('notes-title'), { target: { value: 'Mercado' } });
    fireEvent.change(screen.getByTestId('notes-tags'), { target: { value: 'casa, compras' } });
    fireEvent.click(screen.getByTestId('notes-create'));

    await waitFor(() => expect(screen.getByText('Mercado')).toBeTruthy());
    expect(harness.calls[1].init.body).toBe('{"title":"Mercado","tags":["casa","compras"]}');
  });

  it('exige titulo antes de criar', async () => {
    const harness = testHarness([{ body: '[]' }]);
    render(<NotesScreen />, { wrapper: harness.wrapper });
    await waitFor(() => expect(screen.getByText('Sem notas')).toBeTruthy());

    fireEvent.click(screen.getByTestId('notes-create'));

    expect(screen.getByText('Informe um título')).toBeTruthy();
    expect(harness.calls).toHaveLength(1);
  });
});
