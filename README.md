# NEXUS Launcher 3D — corrigido

Versão revisada com interface espacial mais legível, atalhos maiores, dock ampliado, gaveta filtrável e globo OpenGL ES 2.0 com malha mais densa e animação contínua.

## Release automática
1. Envie os arquivos deste ZIP para a raiz do repositório.
2. Crie uma tag, por exemplo `v1.1.0`, e envie-a: `git tag v1.1.0 && git push origin v1.1.0`.
3. O GitHub Actions compila o APK e publica uma Release com o APK anexado.
4. A execução manual em Actions compila o APK, mas a publicação pública é disparada por tags `v*`.

## Observações
O globo é uma esfera OpenGL com textura estilizada gerada pelo código, não um modelo terrestre cartográfico de alta precisão. O APK release usa assinatura debug para facilitar testes; distribuição ampla deve usar uma chave própria. O projeto foi montado, mas não foi compilado nem testado em aparelho nesta sessão.
