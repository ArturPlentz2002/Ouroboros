/** @jest-environment jsdom */
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import React from 'react';
import { testHarness } from '../../testing/renderWithProviders';
import { FinanceScreen } from './FinanceScreen';

const entryJson =
  '{"id":"e1","categoryId":null,"type":"EXPENSE","amount":150.5,"description":"feira",' +
  '"occurredOn":"2026-06-05","eventId":null,"createdAt":"2026-06-05T12:00:00Z"}';
const summaryJson =
  '{"month":"2026-06","totalIncome":3000,"totalExpense":150.5,"balance":2849.5,"byCategory":[]}';

/** O ScrollView monta summary e entries; as respostas seguem a ordem das chamadas. */
function harnessFor(entriesBody: string, rest: Array<{ status?: number; ok?: boolean; body: string }> = []) {
  return testHarness([{ body: summaryJson }, { body: entriesBody }, ...rest]);
}

describe('FinanceScreen', () => {
  it('mostra o resumo mensal e os lancamentos', async () => {
    const harness = harnessFor(`[${entryJson}]`);
    render(<FinanceScreen />, { wrapper: harness.wrapper });

    await waitFor(() => expect(screen.getByText('Resumo de 2026-06')).toBeTruthy());
    expect(screen.getByText('Saldo: R$ 2849,50')).toBeTruthy();
    await waitFor(() => expect(screen.getByText('feira')).toBeTruthy());
  });

  it('valida valor invalido sem chamar a API de criacao', async () => {
    const harness = harnessFor('[]');
    render(<FinanceScreen />, { wrapper: harness.wrapper });
    await waitFor(() => expect(screen.getByText('Sem lançamentos')).toBeTruthy());

    fireEvent.change(screen.getByTestId('finance-amount'), { target: { value: '-1' } });
    fireEvent.click(screen.getByTestId('finance-create'));

    expect(screen.getByText('Informe um valor maior que zero')).toBeTruthy();
    expect(harness.calls).toHaveLength(2);
  });

  it('cria lancamento e atualiza resumo + lista', async () => {
    const harness = harnessFor('[]', [
      { status: 201, body: entryJson },
      { body: `[${entryJson}]` },
      { body: summaryJson },
    ]);
    render(<FinanceScreen />, { wrapper: harness.wrapper });
    await waitFor(() => expect(screen.getByText('Sem lançamentos')).toBeTruthy());

    fireEvent.change(screen.getByTestId('finance-amount'), { target: { value: '150,50' } });
    fireEvent.change(screen.getByTestId('finance-occurred-on'), {
      target: { value: '2026-06-05' },
    });
    fireEvent.click(screen.getByTestId('finance-create'));

    await waitFor(() => expect(screen.getByText('feira')).toBeTruthy());
    const createCall = harness.calls[2];
    expect(createCall.init.method).toBe('POST');
    expect(createCall.init.body).toBe('{"type":"EXPENSE","amount":150.5,"occurredOn":"2026-06-05"}');
  });

  it('alterna o tipo para receita', async () => {
    const harness = harnessFor('[]', [
      { status: 201, body: entryJson },
      { body: `[${entryJson}]` },
      { body: summaryJson },
    ]);
    render(<FinanceScreen />, { wrapper: harness.wrapper });
    await waitFor(() => expect(screen.getByText('Sem lançamentos')).toBeTruthy());

    fireEvent.click(screen.getByTestId('finance-type-income'));
    fireEvent.change(screen.getByTestId('finance-amount'), { target: { value: '3000' } });
    fireEvent.change(screen.getByTestId('finance-occurred-on'), {
      target: { value: '2026-06-01' },
    });
    fireEvent.click(screen.getByTestId('finance-create'));

    await waitFor(() => expect(harness.calls.length).toBeGreaterThanOrEqual(3));
    expect(harness.calls[2].init.body).toContain('"type":"INCOME"');
  });
});
