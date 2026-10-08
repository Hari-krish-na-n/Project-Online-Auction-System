package com.auction.model;

/**
 * User is the PARENT (base) class for all user types.
 *
 * INHERITANCE: Admin, Seller, and Bidder all extend this class.
 * They inherit userId, name, email, and password fields automatically.
 *
 * POLYMORPHISM: displayRole() is declared abstract here and OVERRIDDEN
 * in each child class so each prints its own role name.
 */
public abstract class User {

    // private fields — can only be accessed via getters/setters (Encapsulation)
    private int    userId;
    private String name;
    private String email;
    private String password;

    // ── Constructor ──────────────────────────────────────────────────────────
    public User(int userId, String name, String email, String password) {
        this.userId   = userId;
        this.name     = name;
        this.email    = email;
        this.password = password;
    }

    // ── Abstract method ──────────────────────────────────────────────────────
    /**
     * Every subclass MUST override this method.
     * This is the foundation of POLYMORPHISM in this project.
     */
    public abstract void displayRole();

    // ── Getters ──────────────────────────────────────────────────────────────
    public int    getUserId()  { return userId;   }
    public String getName()    { return name;     }
    public String getEmail()   { return email;    }
    public String getPassword(){ return password; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setName(String name)         { this.name     = name;     }
    public void setEmail(String email)       { this.email    = email;    }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "User[id=" + userId + ", name=" + name + ", email=" + email + "]";
    }
}