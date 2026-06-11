import { TABS, useNavigationStore } from './navigationStore';

describe('navigationStore', () => {
  beforeEach(() => useNavigationStore.setState({ tab: 'agenda' }));

  it('inicia na agenda', () => {
    expect(useNavigationStore.getState().tab).toBe('agenda');
  });

  it('troca de aba', () => {
    useNavigationStore.getState().setTab('finance');
    expect(useNavigationStore.getState().tab).toBe('finance');
  });

  it('expoe as tres abas do MVP', () => {
    expect(TABS.map((t) => t.key)).toEqual(['agenda', 'finance', 'notes']);
  });
});
