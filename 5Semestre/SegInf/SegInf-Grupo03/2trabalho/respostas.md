# 1

## 1.1 - Quais as passwords que foram quebradas? Destas, qual demorou mais tempo? Houve diferença significativa de tempo para quebra das passwords entre as diferentes funções de hash?

### Bcrypt

Ao executarmos o john foram quebradas três passwords:
 - eva - 123456
 - bob - 123456789
 - carol - password

As passwords foram quebradas em milissegundos entre si, uma vez que o custo do bcrypt é apenas 5 e estas passwords são fracas.
A password mais lenta a ser quebrada foi a password da eva.


### MD5

Ao executarmos o john foram quebradas três passwords:
 - eva - 123456
 - bob - 123456789
 - carol - password

As passwords foram quebradas em milissegundos entre si. A password mais lenta a ser quebrada foi a password da eva.
Não foram observadas diferenças significativas de tempo entre funções de hash.

## 1.2 - Alguma password adicional foi quebrada com esta versão do comando? Houve alguma password que não foi quebrada em nenhum dos dois ataques?

Com esta nova versão do comando, a password da trudy e da alice foram quebradas. A password da mallory continuam por quebrar.

## 1.3 - Este comando é bem-sucedido na quebra da password da utilizadora mallory? Se sim, o tempo necessário é significativo? Alguma outra password é quebrada por ele? Justifique.

Sim, em ambas as execuções (sobre bcrypt.txt e md5.txt) o john encontrou uma correspondência. Confirmando que a passoword encontrava-se entre as 5 passwords
que se encontram no ficheiro possiblePasswords.lst.

Não o tempo necessário não é significativo, uma vez que, este mesmo é praticamente nulo.

Não, nenhuma das outras passwords é quebrada, uma vez que as suas passwords não se encontra no ficheiro possiblePasswords.lst


# 2

## 2.1 - Recorrendo à informação disponibilizada, explique sucintamente de que forma a aplicação mantém o estado de autenticação.

A aplicação mantém estado através de um cookie de sessão chamado MoodleSession, que contém um id anonimo. Este id permite ao 
servidor associar o pedido HTTP à sessão autenticada armazenada do lado do backend. O cookie é enviado automaticamente em cada pedido,
e possui flags de segurança como HttpOnly e Secure, garantindo que o browser permance autenticado enquanto o cookie existir.

## 2.2 - De que forma poderia ser usado outro cliente (por exemplo, postman ou curl) para aceder ao seu perfil com as disciplinas que estão a decorrer https://2526moodle.isel.pt/my/?

Para aceder ao meu perfil através do curl ou postman basta enviar, no cabeçalo HTTP, o cookie de sessão ModdleSession, que o Moodle utiliza para manter
a autenticação. Ao enviar esse cookie manualmente num pedido GET a https://2526moodle.isel.pt/my/ , o servidor reconhece o mesmo utilizador autenticado do browser.
Isto funciona porque o estado da sessáo é guardado no servidor e o cookie apenas transposta o id da sessão.

# 3

## 3.1 - Inspecione os pedidos HTTP registados pelo browser. Em particular, determine os pedidos que correspondem a: 

- O primeiro pedido realizado pelo user-agent ao authorization endpoint.
- O último pedido realizado pelo user-agent ao servidor de autorização.
- O pedido realizado pelo user-agent à callback do Calendly.
    - URI Auth endpoint: https://login.microsoftonline.com/common/oauth2/v2.0/authorize
    - client_id do Calendly: 751ff9b5-edde-4dc1-8093-adf647495745
    - URI Callback: https://calendly.com/users/auth/azure2_oauth2/callback
    - Scope: openid+email+profile+offline_access+https://graph.microsoft.com/User.Read+https://graph.microsoft.com/Calendars.ReadWrite+https://graph.microsoft.com/Calendars.ReadWrite.Shared
    - O parametro state foi utilizado, valor: 908b7026e06ea282e0b467c38b369913844113f56e08640b
    - O parametro code, só é apresentado no pedido de callback. Valor: M.C555_BAY.2.U.02d23d5a-9210-465e-1540-1671d973b716

## 3.2 - Repita a alínea anterior, mas agora realizando o login com uma conta Google.

- URI Auth endpoint: https://accounts.google.com/v3/signin/accountchooser
- client_id do Calendly: 797340822162.apps.googleusercontent.com
- URI Callback: https://calendly.com/users/auth/google_oauth2/callback
- Scope: 
  - No primeiro pedido ao authorization endpoint: email+profile+https://www.googleapis.com/auth/calendar
  - No pedido de callback: email profile https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email openid
- O parametro state foi utilizado, valor: e1da99902960d889f323318cd5ea78495eadebb8921f6039
- O parametro code, só é apresentado no pedido de callback. Valor: 4/0Ab32j92IlwBZmRj1oxRANdk6GSDYtazH-59Fuk0je7ePmv03H2LSbXUBy-WmfYutmESRsw

## 3.3 - Compare as respostas das alíneas anteriores. O client_id, a lista de scopes solicitados e a URI de callback foram iguais? Justifique.

- Client_id
  - Não é igual, uma vez que cada provedor de autenticação (Microsoft e Google) gera o seu próprio registo de aplicações e,
  por isso, atribui um client_id único para identificar a aplicação no respetivo ecossistema. Assim, mesmo sendo
  a mesma aplicação (Calendly), o client_id difere entre os provedores.

- Scopes
  - Não são iguais, uma vez que cada provedor tem APIs prórprias e diferentes. 
  - Por esse motivo, os scopes solicitados variam:
    - No caso da Microsoft, usam-se scopes associados ao Microsoft Graph.
    - No caso da Google, usam-se scopes associados às Google APIs.
  - Embora ambos os conjuntos de scopes incluam permissões para aceder a informações básicas do perfil e ao calendário,
  as APIs subjacentes são distintas, o que justifica as diferenças nos scopes solicitados

- URI de callback
  - Cada provedor usa um endpoint de callback diferente, embora pertençam ao Calendly. O Calendly tem endpoints de 
  callback distintos para cada provedor de autenticação (Microsoft e Google) para manter assim a separação institucional e 
  permitir configurar comportamentos diferentes.

## 3.4 - Nos pedidos e respostas registados pelo browser, é possível encontrar o access token ou o id token? Justifique.

Não. Nos pedidos e respostas registados pelo browser não é possível encontrar o access token ou o id token.

Isto acontece porque o processo usa o fluxo de autorização com código (Authorization Code Flow), no qual o browser apenas recebe 
o parametro "code" (e o "state") na resposta de callback. O access token e o id token são obtidos posteriormente pelo backend do Calendly,
através de um pedido direto ao servidor de autorização. Deste modo, os tokens não são expostos ao browser, garantindo maior segurança
e evitando que estes valores sensíveis fiquem expostos ao utilizador ou a scripts no frontend.

# 4

## Resposta direta

Não, não é possível usar (s_2) para aceder ao recurso X que exige a permissão (p_e).

## Explicação

Pelo UA, (u_2) tem a role (r_2). Como foram ativadas todas as roles de (u_2), o conjunto de roles 
activas em (s_2) é ({r_2}).

A role (r_2) tem associada a permissão (p_c), via PA. A hierarquia de roles indica que (r_2) herda as permissões de (r_0).
Portanto, segundo PA, (r_2) também dispõe da permissão (p_a), assim, (r_2) tem como permissões {(p_c), (p_a)}. Por 
transitividade, como (r_0 \preceq r_2 \preceq r_4), (r_4) herda as permissões de (r_0) e (r_2), 
ou seja, (r_4) tem como permissões {(p_a), (p_b), (p_c)}, mas isto não dá a (r_2) as permissões de (r_4).
Logo, (r_2) não tem a permissão (p_e).

Como a permissão (p_e) não está no conjunto de permissões de (r_2), então (s_2) não consegue aceder
ao recurso X que exige a permissão (p_e).