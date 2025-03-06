package com.sigmap.sigmap_params;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

// EXAMPLE KEYCLOAK JWT TOKEN CLAIM
// {
//   "exp": 1739884603,
//   "iat": 1739884303,
//   "auth_time": 1739884303,
//   "jti": "41254945-1b7c-40de-9d6b-3fd250872887",
//   "iss": "http://localhost:8080/realms/sigmap-realm",
//   "aud": [
//     "sigmap-paa",
//     "account"
//   ],
//   "sub": "bfeaaa8b-514d-47b0-9171-52feb92aa7b6",
//   "typ": "Bearer",
//   "azp": "sigmap-web",
//   "sid": "18068d84-44a3-4200-a28a-32fdf96177af",
//   "acr": "1",
//   "allowed-origins": [
//     "*"
//   ],
//   "realm_access": {
//     "roles": [
//       "offline_access",
//       "default-roles-sigmap-realm",
//       "uma_authorization"
//     ]
//   },
//   "resource_access": {
//     "sigmap-paa": {
//       "roles": [
//         "test_role"
//       ]
//     },
//     "account": {
//       "roles": [
//         "manage-account",
//         "manage-account-links",
//         "view-profile"
//       ]
//     }
//   },
//   "scope": "openid email profile",
//   "email_verified": false,
//   "name": "Haytham Boussarsar",
//   "preferred_username": "superadmin@email.com",
//   "given_name": "Haytham",
//   "family_name": "Boussarsar",
//   "email": "superadmin@email.com"
// }

/** Converts a JWT into a Spring Security authentication token. */
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {
  private final String clientId;

  /**
   * Constructor.
   *
   * @param clientId server client id
   */
  public KeycloakJwtConverter(String clientId) {
    this.clientId = clientId;
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt source) {
    return new JwtAuthenticationToken(source, getAuthorities(source), extractPrincipal(source));
  }

  private String extractPrincipal(Jwt jwt) {
    return jwt.getSubject();
  }

  private Collection<? extends GrantedAuthority> getAuthorities(Jwt jwt) {
    Collection<? extends GrantedAuthority> authorities =
        Stream.concat(extractRealmRoles(jwt).stream(), extractResourceAccessRoles(jwt).stream())
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .toList();

    System.out.println("Authorities " + authorities);
    return authorities;
  }

  /**
   * Extracts the roles from the realm_access claim of the JWT.
   *
   * @param jwt the JWT
   * @return the roles
   */
  private List<String> extractRealmRoles(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    if (realmAccess == null) {
      return List.of();
    }

    System.out.println("Realm access" + realmAccess);

    return (List<String>) realmAccess.get("roles");
  }

  /**
   * Extracts the roles from the resource_access claim of the JWT.
   *
   * @param jwt the JWT
   * @return the roles
   */
  private List<String> extractResourceAccessRoles(Jwt jwt) {
    Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

    System.out.println("Resource access >>" + resourceAccess);

    if (resourceAccess == null
        || resourceAccess.isEmpty()
        || !resourceAccess.containsKey(clientId)) {
      return List.of();
    }

    System.out.println("Resource access " + resourceAccess);
    Map<String, Object> targetClient = (Map<String, Object>) resourceAccess.get(clientId);

    return (List<String>) targetClient.get("roles");
  }
}
