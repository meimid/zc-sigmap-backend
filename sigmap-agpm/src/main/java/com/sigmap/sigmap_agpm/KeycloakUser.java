package com.sigmap.sigmap_agpm;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

/** Map keycloak jwt claims to a user object. */
public class KeycloakUser implements UserDetails {

  private final String userId;

  private final String username;

  private final String email;

  private final Collection<? extends GrantedAuthority> authorities;

  /**
   * Creates a new keycloak user.
   *
   * @param jwt the jwt
   */
  public KeycloakUser(Jwt jwt) {
    this.userId = jwt.getSubject();
    this.username = jwt.getClaimAsString("preferred_username");
    this.email = jwt.getClaimAsString("email");
    this.authorities = this.extractAuthorities(jwt);
  }

  private Collection<? extends GrantedAuthority> extractAuthorities(Jwt jwt) {
    List<String> roles = jwt.getClaim("roles");

    List<? extends GrantedAuthority> authorities =
        roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());

    return authorities;
  }

  public String getUserId() {
    return userId;
  }

  public String getEmail() {
    return email;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return username;
  }
}
