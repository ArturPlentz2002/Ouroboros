import { ApiClient } from '../../api/client';

export interface AgendaEvent {
  id: string;
  title: string;
  description: string | null;
  startsAt: string;
  endsAt: string | null;
  createdAt: string;
}

export interface AgendaEventInput {
  title: string;
  description?: string;
  startsAt: string;
  endsAt?: string;
}

const BASE = '/api/v1/agenda/events';

/** Cliente da agenda (espelha o service-agenda), escopado pelo JWT do {@link ApiClient}. */
export class AgendaApi {
  constructor(private readonly client: ApiClient) {}

  list(): Promise<AgendaEvent[]> {
    return this.client.get<AgendaEvent[]>(BASE);
  }

  get(id: string): Promise<AgendaEvent> {
    return this.client.get<AgendaEvent>(`${BASE}/${id}`);
  }

  create(input: AgendaEventInput): Promise<AgendaEvent> {
    return this.client.post<AgendaEvent>(BASE, input);
  }

  update(id: string, input: AgendaEventInput): Promise<AgendaEvent> {
    return this.client.put<AgendaEvent>(`${BASE}/${id}`, input);
  }

  remove(id: string): Promise<void> {
    return this.client.delete<void>(`${BASE}/${id}`);
  }
}
