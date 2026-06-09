# 1.0.0 (2026-06-09)


### Bug Fixes

* **DDE-030:** rotacao de refresh token atomica (corrige race condition) ([#20](https://github.com/ArturPlentz2002/Ouroboros/issues/20)) ([28e7bd9](https://github.com/ArturPlentz2002/Ouroboros/commit/28e7bd989e0287c8f8678ba7c6bdf17434bc86df))
* **DDE-053:** normaliza nome de categoria e devolve 409 em conflito de unicidade ([#29](https://github.com/ArturPlentz2002/Ouroboros/issues/29)) ([e488eca](https://github.com/ArturPlentz2002/Ouroboros/commit/e488eca7fb2401de2dbb02fc2cb0714d994c0c3b))


### Features

* **DDE-021:** service-auth com registro (BCrypt), Flyway e testes ([#13](https://github.com/ArturPlentz2002/Ouroboros/issues/13)) ([2701e85](https://github.com/ArturPlentz2002/Ouroboros/commit/2701e856d499ccc777dea525ac81dc11996e8d36))
* **DDE-023:** login, refresh rotacionado, JWT RS256 e JWKS ([#14](https://github.com/ArturPlentz2002/Ouroboros/issues/14)) ([c7c3e8f](https://github.com/ArturPlentz2002/Ouroboros/commit/c7c3e8fc5fa4177e722cb909df766e30b17696cd))
* **DDE-024:** service-agenda com CRUD de eventos protegido por JWT ([#16](https://github.com/ArturPlentz2002/Ouroboros/issues/16)) ([196060d](https://github.com/ArturPlentz2002/Ouroboros/commit/196060d2c1eb950593272c3c7630b0cd6cf79469))
* **DDE-026:** adiciona gateway com rotas, CORS e validacao JWT de borda ([#17](https://github.com/ArturPlentz2002/Ouroboros/issues/17)) ([79726ab](https://github.com/ArturPlentz2002/Ouroboros/commit/79726abcce7e0c98d64aac7ff200b8880e1d07b0))
* **DDE-031:** adiciona e valida audience (aud) nos JWT ([#21](https://github.com/ArturPlentz2002/Ouroboros/issues/21)) ([c06889b](https://github.com/ArturPlentz2002/Ouroboros/commit/c06889b6b9122bbbde093a5e6bf48885b2766855))
* **DDE-040:** adiciona login social com Google via /auth/social ([#23](https://github.com/ArturPlentz2002/Ouroboros/issues/23)) ([fe9f704](https://github.com/ArturPlentz2002/Ouroboros/commit/fe9f7045b7b48f3465a81781723dc3d1e02a55cc))
* **DDE-042:** adiciona service-user com perfil sob demanda via JWT ([#25](https://github.com/ArturPlentz2002/Ouroboros/issues/25)) ([71bfcdf](https://github.com/ArturPlentz2002/Ouroboros/commit/71bfcdf3999a43ac07dc93cde90cd0bbff271664))
* **DDE-043:** adiciona logout que revoga o refresh token ([#24](https://github.com/ArturPlentz2002/Ouroboros/issues/24)) ([c4797d7](https://github.com/ArturPlentz2002/Ouroboros/commit/c4797d73051bf108c1a4381276f723fcc53bec30))
* **DDE-050:** adiciona service-finance com crud de categorias ([#26](https://github.com/ArturPlentz2002/Ouroboros/issues/26)) ([ec6d6bc](https://github.com/ArturPlentz2002/Ouroboros/commit/ec6d6bc4f85b0fc1c867dfb0e630501f96be9f3e))
* **DDE-051:** adiciona crud de lancamentos financeiros ([#27](https://github.com/ArturPlentz2002/Ouroboros/issues/27)) ([ada155c](https://github.com/ArturPlentz2002/Ouroboros/commit/ada155ce4b463d8eb0b3358726c5c72685673d76))
* **DDE-052:** adiciona resumo mensal de financas ([#28](https://github.com/ArturPlentz2002/Ouroboros/issues/28)) ([d92f256](https://github.com/ArturPlentz2002/Ouroboros/commit/d92f256b1b214074bdd6f212005eafc7550018ec))
