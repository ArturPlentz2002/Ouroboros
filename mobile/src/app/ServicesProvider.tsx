import React, { createContext, useContext } from 'react';
import { AppServices } from './services';

const ServicesContext = createContext<AppServices | null>(null);

export interface ServicesProviderProps {
  services: AppServices;
  children: React.ReactNode;
}

/** Disponibiliza a raiz de composicao ({@link AppServices}) para os hooks das telas. */
export function ServicesProvider({ services, children }: ServicesProviderProps): React.ReactElement {
  return <ServicesContext.Provider value={services}>{children}</ServicesContext.Provider>;
}

export function useServices(): AppServices {
  const services = useContext(ServicesContext);
  if (!services) {
    throw new Error('useServices requer um <ServicesProvider> acima na arvore');
  }
  return services;
}
