package com.sigmap.sigmap_webmaster;

import java.util.*;
import java.util.stream.Stream;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;


/** Converts a JWT into a Spring Security authentication token. */
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {
  private final List<String> clientIds;

  /**
   * Constructor.
   *
   * @param clientIds server client id
   */
  public KeycloakJwtConverter(List<String> clientIds) {
    this.clientIds = clientIds == null ? List.of() : List.copyOf(clientIds);
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
    Object claim = jwt.getClaim("resource_access");
    if (!(claim instanceof Map)) return List.of();
    Map<String, Object> resourceAccess = (Map<String, Object>) claim;
    System.out.println("Resource access >>" + resourceAccess);

    Set<String> roles = new LinkedHashSet<>();
    for (String clientId : clientIds) {
      if (clientId == null || clientId.isBlank()) continue;
      Object clientObj = resourceAccess.get(clientId);
      if (!(clientObj instanceof Map)) continue;
      Map<String, Object> clientMap = (Map<String, Object>) clientObj;
      Object rolesObj = clientMap.get("roles");
      if (rolesObj instanceof List) {
        ((List<?>) rolesObj).stream().filter(Objects::nonNull).map(Object::toString).forEach(roles::add);
      }
    }
    return List.copyOf(roles);
  }
}
