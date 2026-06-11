import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import React from 'react';
import { AppServices, createServices } from '../app/services';
import { ServicesProvider } from '../app/ServicesProvider';
import { InMemoryTokenStore } from '../auth/tokenStore';
import { Captured, FakeResponse, fakeFetch } from './fakeHttp';

export interface TestHarness {
  services: AppServices;
  queryClient: QueryClient;
  calls: Captured[];
  /** Wrapper para `render`/`renderHook` com QueryClient + Services de teste. */
  wrapper: (props: { children: React.ReactNode }) => React.ReactElement;
}

/** Monta servicos reais sobre um fetch falso, prontos para testes de hooks e telas. */
export function testHarness(responses: FakeResponse[] = [{}]): TestHarness {
  const { fetchFn, calls } = fakeFetch(responses);
  const services = createServices({
    config: { apiBaseUrl: 'http://gw' },
    tokenStore: new InMemoryTokenStore(),
    fetchFn,
  });
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <ServicesProvider services={services}>{children}</ServicesProvider>
    </QueryClientProvider>
  );
  return { services, queryClient, calls, wrapper };
}
