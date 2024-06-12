## Aplicativo de Gerenciamento de Lista de Contatos

### Escopo do Projeto
* Elaborar uma agenda de contatos simples, contendo alguns campos que são :
  *   Nome, CPF, Data de Nascimento, Data/Hora do Cadastro, UF e telefones.
- Caso a UF seja de SP, o campo de CPF será de preenchimento obrigatório;
- Caso a UF seja de MG, não permitir cadastrar clientes menores de 18 anos; 

### 1 - Arquitetura Utilizada
![image](https://github.com/pninci13/contacts-app/assets/69252953/0d8f5177-864c-4bc6-a23d-0daa9f5ade5a)

### 2 - Modularização do Projeto
- Data
  * Contém tudo que envolve dados, acesso, leitura e outros
  * Definição da Entidade de Contato com os campos necessários
  * Um DAO com métodos para acessar o banco
  * Definição do DB
 
- Repository
  - Definição de uma camada entre o DAO e ViewModel
 
- Adapter
  - Adapta os dados que seerão exibidos de cada contato no RecyclerView (Liga as Views)

- ViewModel
  - Fornece os dados a view (UI)

### 3 - Tecnologias e Recursos Usados
* Kotlin como linguagem
* Room database (Armazenamento Local)
  * É possível ver a tabela se rodar o app e ir em : app inspection > contacts_database > tabela
* Material Design para UI, para os campos de input.
* KSP (Kotlin Symbol Processing) - Para as annotations usadas
* Recycler View e Card View - Para mostra a lista de contatos e cada contato em um card
* Live Data - Gerenciamento de dados observavéis
* Coroutines - Para operações assíncronas, como acessar o banco com as operações CRUD (Create, Read, Update e Delete)
* Type Converters - Para poder converter alguns tipos de dados, para dados que o Room pode manter no banco.

### 4 - Possíveis Melhorias
* Melhorar a UI, acredito que possa ser muito mais responsiva
* Fazer outras validações nos inputs, como se o CPF foi digitado no formato XXX.XXX.XXX-XX (Atualmente so valida o numero de caracteres) e se a data também foi digitada correta.
* Adicionar Compose, para uma UI mais "atualizada" - Escolhi usar Material pela familiariedade
* Integrar uma API para pegar os UFs e não hard coded
* Integração com Firebase para um autenticação de cada usuário
* Implementação de Testes unitário e testes instrumentados.


Fiz o projeto de acordo com meu conhecimento e tentei sempre manter boas práticas, como modularização, organização, código comentado e outros ... =)
