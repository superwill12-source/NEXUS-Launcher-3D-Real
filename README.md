# NEXUS Launcher 3D — Android

Versão experimental com globo terrestre 3D renderizado em OpenGL ES 2.0. O planeta é uma esfera 3D texturizada, com iluminação por shader, rotação contínua e controle por arraste; as órbitas são linhas 3D. A interface Android sobrepõe relógio, painéis de vidro, pesquisa, atalhos e gaveta de apps.

## Recursos nesta versão
- Globo 3D real (malha esférica OpenGL ES 2.0), textura terrestre própria e iluminação de shader.
- Rotação automática e interação por arraste do dedo.
- Anéis orbitais 3D.
- Tela inicial NEXUS com painéis translúcidos, atalhos, relógio e dock.
- Gaveta que enumera apps instalados e permite iniciá-los.
- Pesquisa de apps e fallback para pesquisa web.
- Telas de personalização, visão do mundo e pasta orbital.
- Ícone do app e wallpaper espacial incluídos.
- GitHub Actions para compilar APK automaticamente.

## O que ainda não é uma implementação completa
A esfera e os anéis são 3D reais, mas ícones orbitais desenhados sobre a tela são overlays 2D, não objetos 3D presos à superfície do globo. Não há dados meteorológicos reais, IA integrada, controle de notificações, widgets externos nem janelas flutuantes de outros apps; essas funções requerem desenvolvimento adicional e/ou permissões específicas. O resultado procura fidelidade visual ao conceito, mas não é pixel a pixel idêntico à arte promocional.

## Compilar no GitHub pelo celular
1. Crie um repositório `NEXUS-Launcher-3D`.
2. Envie todos os arquivos deste ZIP para a raiz.
3. Entre em **Actions** e abra `Build NEXUS Launcher 3D APK`.
4. Baixe o artefato `NEXUS-Launcher-3D-APK` após a compilação.
5. Instale o APK e selecione NEXUS Launcher como app padrão de início.

## Requisitos
Android 6.0 ou posterior (API 23+). A compilação usa Android Gradle Plugin 8.7.3 e Java 17.
