package com.auction.model;

/**
 * User is the abstract parent class for all user types in the Online Auction System.
 *
 * INHERITANCE:
 * - Extends BaseEntity to inherit common entity attributes ('id' and 'createdAt').
 * - Admin, Seller, and Bidder extend User to inherit common account fields (name, email, password).
 *
 * POLYMORPHISM:
 * - Declares displayRole() and getRoleDescription() which subclasses override to provide role-specific behavior.
 */
public abstract class User extends BaseEntity {

    // private fields — can only be accessed via getters/setters (Encapsulation)
    private String name;
    private String email;
    private String password;

    // ── Constructor ──────────────────────────────────────────────────────────
    public User(int userId, String name, String email, String password) {
        // super(userId) calls the parent constructor (BaseEntity)
        super(userId);
        this.name     = name;
        this.email    = email;
        this.password = password;
    }

    // ── Abstract & Overridable Methods for Polymorphism ───────────────────────
    /**
     * Every subclass MUST override this method.
     * Demonstrates runtime polymorphism when invoked via a User reference.
     */
    public abstract void displayRole();

    /**
     * Returns a human-readable description of the user's role and privileges.
     * Subclasses override this to describe their capabilities.
     *
     * @return role description string
     */
    public abstract String getRoleDescription();

    // ── Getters ──────────────────────────────────────────────────────────────
    public int    getUserId()   { return getId(); } // delegates to BaseEntity.getId()
    public String getName()     { return name; }
    public String getEmail()    { return email; }
    public String getPassword() { return password; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setName(String name)         { this.name     = name; }
    public void setEmail(String email)       { this.email    = email; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "User[id=" + getId() + ", name=" + name + ", email=" + email + ", createdAt=" + getCreatedAt() + "]";
    }
}