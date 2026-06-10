import { ApiClient } from '../../api/client';

export interface Note {
  id: string;
  title: string;
  content: string | null;
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export interface NoteInput {
  title: string;
  content?: string;
  tags?: string[];
}

export interface NoteQuery {
  tag?: string;
  q?: string;
}

const BASE = '/api/v1/notes';

/** Cliente das notas (espelha o service-notes), escopado pelo JWT do {@link ApiClient}. */
export class NotesApi {
  constructor(private readonly client: ApiClient) {}

  list(query: NoteQuery = {}): Promise<Note[]> {
    const params = new URLSearchParams();
    if (query.tag) {
      params.set('tag', query.tag);
    }
    if (query.q) {
      params.set('q', query.q);
    }
    const qs = params.toString();
    return this.client.get<Note[]>(qs ? `${BASE}?${qs}` : BASE);
  }

  get(id: string): Promise<Note> {
    return this.client.get<Note>(`${BASE}/${id}`);
  }

  create(input: NoteInput): Promise<Note> {
    return this.client.post<Note>(BASE, input);
  }

  update(id: string, input: NoteInput): Promise<Note> {
    return this.client.put<Note>(`${BASE}/${id}`, input);
  }

  remove(id: string): Promise<void> {
    return this.client.delete<void>(`${BASE}/${id}`);
  }
}
