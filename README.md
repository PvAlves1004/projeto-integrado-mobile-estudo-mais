# Estudo+

Aplicativo mobile Android para gestão de tarefas acadêmicas. O administrador cadastra usuários e turmas, os professores publicam tarefas por turma e os alunos acompanham prazos e notas pelo app.

> Projeto Integrador — Engenharia de Software — UNICEPLAC

## Funcionalidades

- **Admin:** cria logins (sem autocadastro), turmas e vincula professores e alunos
- **Professor:** publica tarefas (nome, descrição, data e nota opcional) para suas turmas
- **Aluno:** acompanha tarefas, prazos e notas das turmas em que está matriculado
- Login com recuperação de senha

## Tecnologias

- Java
- Android Studio
- Firebase Authentication
- Cloud Firestore

## Estrutura do repositório

| Pasta | Conteúdo |
|---|---|
| `/app` | Código-fonte do aplicativo |
| `/docs` | Documentação (wireframes, casos de uso, diagramas, regras de negócio) |
| `/assets` | Recursos estáticos (ícones, logos, imagens) |
| `/tests` | Artefatos de teste (os testes do código ficam em `app/src/test` e `app/src/androidTest`) |
| `/.github` | Template de Pull Request |

## Como rodar o projeto

1. Clone o repositório:
```bash
   git clone https://github.com/PvAlves1004/projeto-integrado-mobile-estudo-mais.git
```
2. Abra a pasta no Android Studio
3. Solicite o arquivo `google-services.json` a um membro da equipe e coloque em `/app` (ele não é versionado por segurança)
4. Aguarde a sincronização do Gradle
5. Execute em um emulador ou dispositivo com Android 7.0 (API 24) ou superior

## Fluxo de trabalho (Git Flow simplificado)

- `main` — versão estável, recebe código apenas via Pull Request aprovado
- `develop` — branch de integração
- `feature/*`, `docs/*` — branches de trabalho criadas a partir da `develop`

Todo merge em `main` e `develop` exige Pull Request com aprovação de pelo menos 1 membro.

## Equipe

| Nome | Função |
|---|---|
| Paulo Victor Alves | Back-end / Tech Lead |
| Francisco Flávio | Back-end |
| Gabriel Sousa | Front-end |
| Victor Patrick | Front-end |
| Gabriel Lima | Testes |
| Jean Dolzane | Testes |
| Jamerson Oliveira | Documentação |
| Hicaro Gabryel Santos | Análise de Pontos de Função |
