# Configuração do Banco de Dados

## Baixar

- PostgreSQL 14 ou superior

---

## 1. Criação do banco e usuário da aplicação

Criando o database da prática e criando usuário para a aplicação.

```sql
-- Cria o banco de dados
CREATE DATABASE seguranca_computacional;

-- Cria o usuário com senha
CREATE USER app_user WITH PASSWORD 'SenhaForte123@';

-- Garante conexão ao banco 
GRANT CONNECT ON DATABASE seguranca_computacional TO app_user;
```


```sql
-- Criando a tabela de usuarios
CREATE TABLE users (
    id        SERIAL PRIMARY KEY,
    nome      VARCHAR(100)  NOT NULL,
    email     VARCHAR(150)  UNIQUE NOT NULL,
    senha     VARCHAR(255)  NOT NULL,
    role      VARCHAR(50)   NOT NULL,
    criado_em TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- Permissões garantidas: apenas SELECT, INSERT, UPDATE na tabela users
-- app_user não consegue dar DROP, DELETE ou criar tabelas
GRANT SELECT, INSERT, UPDATE ON TABLE users TO app_user;

-- Permissão para funcionar o SERIAL do ID (gera uma sequence)
GRANT USAGE, SELECT ON SEQUENCE users_id_seq TO app_user;
```

> Seguindo o princípio do menor privilégio, a aplicação só precisa
> ler e escrever dados. Não precisa de outras funções no banco.
> Limitamos a permissão do usuário para evitar danos caso um ataque de 
> SQL Injection seja bem-sucedido.

---

## 2. Variáveis de ambiente

A aplicação não deve ler credenciais de banco direto do `application.yml`.
As senhas e API keys são lidas de variáveis de ambiente (.env) 

### Comandos no Windows 

```powershell
$env:DB_URL      = "jdbc:postgresql://localhost:5432/seguranca_computacional"
$env:DB_USER     = "app_user"
$env:DB_PASSWORD = "SenhaForte123@"
$env:CLIENT_ID   = "google_client_id"
$env:CLIENT_SECRET = "google_client_secret"
```

### IntelliJ IDEA
No IntelliJ, podemos configurar as variáveis de ambiente no próprio projeto:

`Run -> Edit Configurations -> Environment variables`

---

## 3. Proteções contra SQL Injection

A aplicação usa queries parametrizadas via Spring Data JPA.
Nenhuma string SQL é construída por concatenação da entrada do usuário.

### Utilizamos:

```java
// O Framework deriva a query como prepared statement automaticamente
Optional<UserEntity> findByEmail(String email);

// Utilizamos termos genéricos e não concatenamos strings para evitar SQL Injection
@Query("SELECT u FROM UserEntity u WHERE u.role = :role")
List<UserEntity> findByRole(@Param("role") String role);
```

### Não fazemos string "SQL" concatenada com entrada:

```java
String sql = "SELECT * FROM users WHERE email = '" + email + "'";
```

---

## 4. Senhas de usuários

Utilizamos o **Google OAuth2** para autenticar a sessão, a coluna `senha` da tabela
existe para permitir compatibilidade futura com login local, mas fica vazia (`''`)
para usuários OAuth2. Caso implemente login local no futuro:

---

## 5. Testando Rotas

Primeiramente, testamos se as rotas estão funcionando normalmente.

Ao acessar as rotas abaixo, devemos receber respostas em JSON com os dados do usuário,
já que estamos utilizando um `@RestController`:

```
# A primeira rota é a raiz da aplicação, onde há o login por Google OAuth2, 
# as demais são as rotas de consulta de usuários (apenas podem ser acessadas 
# se o usuário estiver logado na sessão).
http://localhost:8080

http://localhost:8080/api/users
http://localhost:8080/api/users/email?value=seu@email.com
http://localhost:8080/api/users/role?value=PUBLIC
http://localhost:8080/api/users/search?nome=Lorenzo
```

### Rota segura implementada
> **GET /api/users/email?value={email}**

Essa rota utiliza query parametrizada via JPA, impedindo SQL Injection.

## 6. Testando SQL Injection

Em sequência, testamos as rotas com payloads de SQL Injection para verificar se a aplicação é vulnerável.

```
# Principal tentativa de ataque, injetando um OR sempre verdadeiro para tentar retornar todos os usuários
http://localhost:8080/api/users/email?value=' OR '1'='1

# Tentativa de comentar o resto da query
http://localhost:8080/api/users/email?value=teste'--

# Tentativa de UNION para extrair dados de uma tabela do banco
http://localhost:8080/api/users/search?nome=' UNION SELECT * FROM users--
```

Com prepared statements, todos esses retornarão 404 Not Found ou lista vazia,
o valor é tratado como texto literal, nunca como SQL.

Caso o ataque ocorresse com sucesso, o primeiro URL retornaria todos os usuários, 
o segundo causaria um erro de sintaxe SQL e permitiria acesso não autorizado, 
e, por fim, o terceiro poderia expor dados sensíveis do banco.

## 7. Validação e Sanitização de Entrada

Além do uso de queries parametrizadas (Prepared Statements), a aplicação também aplica validação de entrada para aumentar a segurança.

### Estratégia:

- Para campos livres (ex: nome, email), utilizamos validação de formato e tamanho (Usamos Jakarta Bean Validation com anotações como @Email, @Size).
- Para campos com domínio controlado (ex: role), utilizamos validação baseada em ENUM (ao invés de receber uma string livre, recebemos um valor do conjunto pré-definido, neste caso: a ENUM SecurityLevel).

### Validação de role

A rota `/api/users/role` recebe um parâmetro do tipo ENUM:

```java
@RequestParam SecurityLevel value