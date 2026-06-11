import { fakeHttp } from '../../testing/fakeHttp';
import { FinanceApi } from './financeApi';

const categoryJson =
  '{"id":"c1","name":"Mercado","color":"#4caf50","createdAt":"2026-06-01T00:00:00Z"}';
const entryJson =
  '{"id":"e1","categoryId":"c1","type":"EXPENSE","amount":150.5,"description":"feira",' +
  '"occurredOn":"2026-06-05","eventId":null,"createdAt":"2026-06-05T12:00:00Z"}';
const summaryJson =
  '{"month":"2026-06","totalIncome":3000,"totalExpense":150.5,"balance":2849.5,' +
  '"byCategory":[{"categoryId":"c1","categoryName":"Mercado","type":"EXPENSE","total":150.5}]}';

describe('FinanceApi — categorias', () => {
  it('lista categorias', async () => {
    const { client, calls } = fakeHttp([{ body: `[${categoryJson}]` }]);
    const categories = await new FinanceApi(client).listCategories();

    expect(calls[0].url).toBe('http://gw/api/v1/finance/categories');
    expect(categories[0].name).toBe('Mercado');
  });

  it('cria via POST com o corpo informado', async () => {
    const { client, calls } = fakeHttp([{ status: 201, body: categoryJson }]);
    await new FinanceApi(client).createCategory({ name: 'Mercado', color: '#4caf50' });

    expect(calls[0].url).toBe('http://gw/api/v1/finance/categories');
    expect(calls[0].init.method).toBe('POST');
    expect(calls[0].init.body).toBe('{"name":"Mercado","color":"#4caf50"}');
  });

  it('atualiza via PUT', async () => {
    const { client, calls } = fakeHttp([{ body: categoryJson }]);
    await new FinanceApi(client).updateCategory('c1', { name: 'Mercado' });

    expect(calls[0].url).toBe('http://gw/api/v1/finance/categories/c1');
    expect(calls[0].init.method).toBe('PUT');
  });

  it('remove via DELETE', async () => {
    const { client, calls } = fakeHttp([{ status: 204, body: '' }]);
    await new FinanceApi(client).removeCategory('c1');

    expect(calls[0].url).toBe('http://gw/api/v1/finance/categories/c1');
    expect(calls[0].init.method).toBe('DELETE');
  });
});

describe('FinanceApi — lancamentos', () => {
  it('lista lancamentos', async () => {
    const { client, calls } = fakeHttp([{ body: `[${entryJson}]` }]);
    const entries = await new FinanceApi(client).listEntries();

    expect(calls[0].url).toBe('http://gw/api/v1/finance/entries');
    expect(entries[0].type).toBe('EXPENSE');
    expect(entries[0].amount).toBe(150.5);
  });

  it('cria via POST com vinculo opcional a evento da agenda', async () => {
    const { client, calls } = fakeHttp([{ status: 201, body: entryJson }]);
    await new FinanceApi(client).createEntry({
      categoryId: 'c1',
      type: 'EXPENSE',
      amount: 150.5,
      occurredOn: '2026-06-05',
      eventId: 'ev1',
    });

    expect(calls[0].url).toBe('http://gw/api/v1/finance/entries');
    expect(calls[0].init.method).toBe('POST');
    expect(calls[0].init.body).toBe(
      '{"categoryId":"c1","type":"EXPENSE","amount":150.5,"occurredOn":"2026-06-05","eventId":"ev1"}',
    );
  });

  it('atualiza via PUT', async () => {
    const { client, calls } = fakeHttp([{ body: entryJson }]);
    await new FinanceApi(client).updateEntry('e1', {
      type: 'INCOME',
      amount: 3000,
      occurredOn: '2026-06-01',
    });

    expect(calls[0].url).toBe('http://gw/api/v1/finance/entries/e1');
    expect(calls[0].init.method).toBe('PUT');
  });

  it('remove via DELETE', async () => {
    const { client, calls } = fakeHttp([{ status: 204, body: '' }]);
    await new FinanceApi(client).removeEntry('e1');

    expect(calls[0].url).toBe('http://gw/api/v1/finance/entries/e1');
    expect(calls[0].init.method).toBe('DELETE');
  });
});

describe('FinanceApi — resumo mensal', () => {
  it('busca o resumo do mes informado', async () => {
    const { client, calls } = fakeHttp([{ body: summaryJson }]);
    const summary = await new FinanceApi(client).summary('2026-06');

    expect(calls[0].url).toBe('http://gw/api/v1/finance/summary?month=2026-06');
    expect(summary.balance).toBe(2849.5);
    expect(summary.byCategory[0].categoryName).toBe('Mercado');
  });

  it('omite o parametro month para usar o mes atual do backend', async () => {
    const { client, calls } = fakeHttp([{ body: summaryJson }]);
    await new FinanceApi(client).summary();

    expect(calls[0].url).toBe('http://gw/api/v1/finance/summary');
  });
});
