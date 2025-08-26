# 🗳️ IBetel Vote

Sistema completo de votação eletrônica para eleições eclesiásticas, desenvolvido especificamente para gerenciar processos eleitorais com regras hierárquicas ministeriais.

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![React](https://img.shields.io/badge/react-%2320232a.svg?style=for-the-badge&logo=react&logoColor=%2361DAFB)
![TypeScript](https://img.shields.io/badge/typescript-%23007ACC.svg?style=for-the-badge&logo=typescript&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/postgresql-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)

## 📋 Sumário

- [🎯 Sobre o Projeto](#-sobre-o-projeto)
- [✨ Funcionalidades](#-funcionalidades)
- [🏗️ Arquitetura](#️-arquitetura)
- [🚀 Tecnologias](#-tecnologias)
- [📦 Instalação](#-instalação)
- [⚙️ Configuração](#️-configuração)
- [🔧 Como Usar](#-como-usar)
- [🗄️ Banco de Dados](#️-banco-de-dados)
- [🔒 Segurança](#-segurança)
- [📡 API](#-api)
- [🎨 Interface](#-interface)
- [📊 Regras de Negócio](#-regras-de-negócio)
- [🧪 Testes](#-testes)
- [📈 Status](#-status)
- [🤝 Contribuindo](#-contribuindo)
- [📄 Licença](#-licença)

## 🎯 Sobre o Projeto

O **IBetel Vote** é uma solução completa para automatizar eleições eclesiásticas, oferecendo um processo seguro, transparente e auditável para a escolha de líderes ministeriais.

### 🎪 Características Principais

- **🔒 Segurança robusta** com autenticação JWT e auditoria completa
- **⛪ Regras eclesiásticas** específicas para hierarquia ministerial
- **🗳️ Votação segura** com suporte a voto branco, nulo e válido
- **📊 Relatórios em tempo real** com estatísticas e rankings
- **👥 Auto-cadastro** para membros da igreja
- **📱 Interface responsiva** e intuitiva

## ✨ Funcionalidades

### 🔐 **Autenticação e Autorização**
- Login com email/senha
- Auto-cadastro de membros (validação CPF/email)
- Sistema de roles hierárquico (Membro → Utilizador Pro → Administrador)
- JWT tokens com renovação automática

### 👥 **Gestão de Membros**
- CRUD completo de membros da igreja
- Upload de fotos de perfil
- Validação de elegibilidade para cargos
- Busca e filtros avançados
- Associação com usuários do sistema

### 💼 **Gestão de Cargos**
- Criação e manutenção de cargos ministeriais
- Controle de cargos disponíveis para eleições
- Hierarquia definida (Obreiro → Diácono → Presbítero → Pastor)

### 🗳️ **Sistema de Eleições**
- Criação de eleições com períodos definidos
- Configurações específicas (voto branco, nulo, resultados parciais)
- Controle de estado (Inativa → Ativa → Encerrada)
- Validações automáticas para ativação

### 👤 **Gestão de Candidatos**
- Cadastro de candidaturas com propostas e experiência
- Workflow de aprovação/reprovação
- Upload de fotos de campanha
- Definição de números de campanha
- Validação automática de elegibilidade

### 🗳️ **Processo de Votação**
- Interface segura de votação
- Validações em tempo real
- Suporte a voto branco e nulo
- Verificação "já votou"
- Hash de segurança e rastreamento

### 📊 **Relatórios e Resultados**
- Estatísticas em tempo real
- Rankings por cargo
- Análise de participação
- Dados para auditoria
- Progresso de votação por hora

## 🏗️ Arquitetura

O sistema segue os princípios do **Domain-Driven Design (DDD)** com separação clara de responsabilidades:

```
📁 Backend (Spring Boot)
├── 📁 domain/           # Entidades e regras de negócio
├── 📁 application/      # DTOs e casos de uso
└── 📁 infrastructure/   # Controllers, repositórios e configurações

📁 Frontend (Next.js)
├── 📁 components/       # Componentes reutilizáveis
├── 📁 hooks/           # Hooks customizados
├── 📁 services/        # Integração com APIs
└── 📁 pages/           # Páginas da aplicação
```

## 🚀 Tecnologias

### Backend
- **Java 17+** - Linguagem principal
- **Spring Boot 3.x** - Framework principal
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Acesso a dados
- **PostgreSQL** - Banco de dados
- **JWT** - Tokens de autenticação
- **Swagger/OpenAPI** - Documentação da API

### Frontend
- **React 18** - Biblioteca de interface
- **Next.js 14** - Framework React
- **TypeScript** - Type safety
- **Tailwind CSS** - Estilização
- **Lucide React** - Ícones

### Infraestrutura
- **PostgreSQL** - Banco de dados principal
- **Caffeine/EhCache** - Sistema de cache
- **Spring Boot Actuator** - Monitoramento

## 📦 Instalação

### Pré-requisitos
- Java 17 ou superior
- Node.js 18 ou superior
- PostgreSQL 12 ou superior
- Git

### 1. Clone o repositório
```bash
git clone https://github.com/seu-usuario/ibetelvote.git
cd ibetelvote
```

### 2. Configuração do Backend
```bash
# Instalar dependências
./mvnw clean install

# Configurar banco de dados no application.properties
# (ver seção de configuração)
```

### 3. Configuração do Frontend
```bash
cd frontend
npm install
```

## ⚙️ Configuração

### 🗄️ Banco de Dados

1. **Criar banco PostgreSQL:**
```sql
CREATE DATABASE ibetelvote;
CREATE USER ibetelvote_user WITH PASSWORD 'sua_senha_aqui';
GRANT ALL PRIVILEGES ON DATABASE ibetelvote TO ibetelvote_user;
```

2. **Configurar application.properties:**
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/ibetelvote
spring.datasource.username=ibetelvote_user
spring.datasource.password=sua_senha_aqui

# JWT
app.security.jwt.secret=sua_chave_secreta_jwt_aqui
app.security.jwt.access-token-expiration-minutes=60
app.security.jwt.refresh-token-expiration-days=7

# Upload
app.upload.path=./uploads
app.upload.max-file-size=5MB
```

### 🔑 Variáveis de Ambiente (Produção)
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/ibetelvote
export DATABASE_USERNAME=ibetelvote_user
export DATABASE_PASSWORD=senha_producao
export JWT_SECRET=chave_secreta_forte_producao
export SERVER_PORT=8081
```

## 🔧 Como Usar

### 🚀 Executar em Desenvolvimento

#### Backend:
```bash
./mvnw spring-boot:run
```
Servidor rodará em: `http://localhost:8081`

#### Frontend:
```bash
cd frontend
npm run dev
```
Aplicação rodará em: `http://localhost:3000`

### 📖 Documentação da API
Acesse: `http://localhost:8081/swagger-ui.html`

### 👤 Primeiro Acesso

1. **Criar usuário administrador** (via SQL ou endpoint específico)
2. **Cadastrar cargos ministeriais** (Obreiro, Diácono, Presbítero, etc.)
3. **Importar/cadastrar membros** da igreja
4. **Criar primeira eleição** e configurar candidatos
5. **Ativar eleição** quando pronta

## 🗄️ Banco de Dados

### 📊 Principais Entidades
- **users** - Usuários do sistema (autenticação)
- **membros** - Membros da igreja
- **cargos** - Cargos ministeriais
- **eleicoes** - Eleições criadas
- **candidatos** - Candidaturas registradas
- **votos** - Votos computados

### 🔗 Relacionamentos
```
User 1:1 Membro
Membro N:1 Cargo (cargo atual)
Candidato N:1 Membro
Candidato N:1 Eleicao
Candidato N:1 Cargo (pretendido)
Voto N:1 Membro
Voto N:1 Eleicao
Voto N:1 Candidato (opcional - pode ser branco/nulo)
```

## 🔒 Segurança

### 🛡️ Implementações
- **Autenticação JWT** com access e refresh tokens
- **Autorização baseada em roles** (RBAC)
- **Validação de dados** em todos os endpoints
- **Hash de senhas** com BCrypt
- **CORS configurado** adequadamente
- **Auditoria completa** de ações
- **Proteção contra SQL Injection**

### 🔐 Roles e Permissões
- **MEMBRO**: Votar, ver eleições ativas, editar próprio perfil
- **UTILIZADOR_PRO**: + Relatórios, consultas avançadas, gestão básica
- **ADMINISTRADOR**: + CRUD completo, configurações do sistema

## 📡 API

### 🌐 Endpoints Principais
```
🔐 Autenticação:
POST /api/v1/auth/login
POST /api/v1/auth/refresh
GET  /api/v1/auth/me

👥 Membros:
GET    /api/v1/membros
POST   /api/v1/membros
GET    /api/v1/membros/{id}
PUT    /api/v1/membros/{id}

🗳️ Eleições:
GET    /api/v1/eleicoes
POST   /api/v1/eleicoes
GET    /api/v1/eleicoes/ativa
POST   /api/v1/eleicoes/{id}/ativar

🗳️ Votação:
POST   /api/v1/votos/votar
GET    /api/v1/votos/eleicao/{id}/resultados
```

### 📝 Documentação Completa
- **Swagger UI**: `http://localhost:8081/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8081/v3/api-docs`
- **150+ endpoints** totalmente documentados

## 🎨 Interface

### 🖥️ Dashboard Principal
- **Votação** - Interface principal de votação
- **Membros** - Gestão de membros da igreja
- **Candidatos** - Gestão de candidaturas
- **Resultados** - Visualização de resultados
- **Histórico** - Consulta eleições anteriores
- **Cargos Ministeriais** - Gestão de cargos
- **Meu Perfil** - Configurações pessoais

### 🎨 Design System
- **Dark theme** moderno e elegante
- **Componentes reutilizáveis** e consistentes
- **Responsividade** para desktop e mobile
- **Acessibilidade** com contraste adequado

## 📊 Regras de Negócio

### ⛪ Hierarquia para Candidaturas
```
👤 Sem cargo     → 🔹 Diácono (apenas)
👤 Obreiro       → 🔹 Diácono (apenas)  
👤 Diácono       → 🔹 Diácono (reeleição) ou 🔹 Presbítero
👤 Presbítero    → 🔹 Presbítero (reeleição apenas)
👤 Pastor        → ❌ Não participa de eleições comuns
```

### 🗳️ Regras de Votação
- **1 voto por membro por cargo por eleição**
- **Elegibilidade**: Membro ativo com dados completos
- **Tipos de voto**: Válido, branco ou nulo
- **Período**: Dentro da janela definida na eleição
- **Auditoria**: Hash de segurança + origem registrada

## 🧪 Testes

### 🔬 Executar Testes
```bash
# Backend
./mvnw test

# Frontend  
cd frontend
npm test
```

### 📊 Cobertura
- Testes unitários para regras de negócio
- Testes de integração para APIs
- Testes de componentes no frontend

## 📈 Status

### ✅ **Implementado**
- [x] Sistema de autenticação completo
- [x] Gestão de membros e usuários
- [x] Sistema de eleições funcionais
- [x] Processo de votação seguro
- [x] Relatórios e estatísticas
- [x] Interface moderna e responsiva
- [x] APIs REST completas (150+ endpoints)
- [x] Documentação Swagger

### 🔄 **Em Desenvolvimento**
- [ ] Testes automatizados abrangentes
- [ ] Otimizações de performance
- [ ] Funcionalidades avançadas de auditoria

### 📋 **Roadmap Futuro**
- [ ] Containerização com Docker
- [ ] Notificações (email/SMS)
- [ ] App mobile nativo
- [ ] Relatórios PDF avançados
- [ ] Backup automatizado
- [ ] Dashboard analytics

## 🤝 Contribuindo

1. **Fork** o projeto
2. **Crie** uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. **Commit** suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. **Push** para a branch (`git push origin feature/MinhaFeature`)
5. **Abra** um Pull Request

### 📝 Padrões de Desenvolvimento
- **Clean Code** e **SOLID** principles
- **Conventional Commits** para mensagens
- **Testes** para novas funcionalidades
- **Documentação** atualizada

## 📞 Contato

- **Projeto**: [IBetel Vote](https://github.com/seu-usuario/ibetelvote)
- **Documentação**: [Wiki do Projeto](https://github.com/seu-usuario/ibetelvote/wiki)
- **Issues**: [GitHub Issues](https://github.com/seu-usuario/ibetelvote/issues)


<div align="center">
  <p>Desenvolvido com ❤️ para a Igreja Evangélica Betel</p>
  <p>
    <strong>iBetel Vote</strong> - Sistema de Votação Eclesiástica
  </p>
</div>