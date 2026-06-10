import { fakeHttp } from '../../testing/fakeHttp';
import { NotesApi } from './notesApi';

const noteJson =
  '{"id":"1","title":"Nota","content":"x","tags":["a"],"createdAt":"2026-01-01T00:00:00Z","updatedAt":"2026-01-01T00:00:00Z"}';

describe('NotesApi', () => {
  it('lista sem filtros', async () => {
    const { client, calls } = fakeHttp([{ body: `[${noteJson}]` }]);
    const notes = await new NotesApi(client).list();

    expect(calls[0].url).toBe('http://gw/api/v1/notes');
    expect(notes).toHaveLength(1);
    expect(notes[0].title).toBe('Nota');
  });

  it('lista com filtros de tag e texto', async () => {
    const { client, calls } = fakeHttp([{ body: '[]' }]);
    await new NotesApi(client).list({ tag: 'work', q: 'leite' });

    expect(calls[0].url).toBe('http://gw/api/v1/notes?tag=work&q=leite');
  });

  it('cria via POST', async () => {
    const { client, calls } = fakeHttp([{ status: 201, body: noteJson }]);
    const created = await new NotesApi(client).create({ title: 'Nota', tags: ['a'] });

    expect(calls[0].url).toBe('http://gw/api/v1/notes');
    expect(calls[0].init.method).toBe('POST');
    expect(created.id).toBe('1');
  });

  it('atualiza via PUT', async () => {
    const { client, calls } = fakeHttp([{ body: noteJson }]);
    await new NotesApi(client).update('1', { title: 'Nova' });

    expect(calls[0].url).toBe('http://gw/api/v1/notes/1');
    expect(calls[0].init.method).toBe('PUT');
  });

  it('remove via DELETE', async () => {
    const { client, calls } = fakeHttp([{ status: 204, body: '' }]);
    await new NotesApi(client).remove('1');

    expect(calls[0].url).toBe('http://gw/api/v1/notes/1');
    expect(calls[0].init.method).toBe('DELETE');
  });
});
