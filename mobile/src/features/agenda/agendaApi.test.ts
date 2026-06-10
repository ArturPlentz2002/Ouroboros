import { fakeHttp } from '../../testing/fakeHttp';
import { AgendaApi } from './agendaApi';

const eventJson =
  '{"id":"1","title":"Reuniao","description":"pauta","startsAt":"2026-07-01T10:00:00Z","endsAt":"2026-07-01T11:00:00Z","createdAt":"2026-06-01T00:00:00Z"}';

describe('AgendaApi', () => {
  it('lista eventos', async () => {
    const { client, calls } = fakeHttp([{ body: `[${eventJson}]` }]);
    const events = await new AgendaApi(client).list();

    expect(calls[0].url).toBe('http://gw/api/v1/agenda/events');
    expect(events[0].title).toBe('Reuniao');
  });

  it('cria via POST com o corpo informado', async () => {
    const { client, calls } = fakeHttp([{ status: 201, body: eventJson }]);
    await new AgendaApi(client).create({ title: 'Reuniao', startsAt: '2026-07-01T10:00:00Z' });

    expect(calls[0].url).toBe('http://gw/api/v1/agenda/events');
    expect(calls[0].init.method).toBe('POST');
    expect(calls[0].init.body).toBe('{"title":"Reuniao","startsAt":"2026-07-01T10:00:00Z"}');
  });

  it('atualiza via PUT', async () => {
    const { client, calls } = fakeHttp([{ body: eventJson }]);
    await new AgendaApi(client).update('1', { title: 'Nova', startsAt: '2026-07-01T10:00:00Z' });

    expect(calls[0].url).toBe('http://gw/api/v1/agenda/events/1');
    expect(calls[0].init.method).toBe('PUT');
  });

  it('remove via DELETE', async () => {
    const { client, calls } = fakeHttp([{ status: 204, body: '' }]);
    await new AgendaApi(client).remove('1');

    expect(calls[0].url).toBe('http://gw/api/v1/agenda/events/1');
    expect(calls[0].init.method).toBe('DELETE');
  });
});
