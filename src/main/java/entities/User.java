package entities;

import java.time.LocalDateTime;

public class User {

    public enum Role {
        CLIENT, VETERINAIRE, PETSITTER, SALON_TOILETTAGE, ADMIN
    }

    public enum Status {
        PENDING_EMAIL, PENDING_ADMIN, APPROVED, REJECTED, SUSPENDED, BANNED
    }

    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private String address;
    private String city;
    private String postalCode;
    private String profilePhoto;
    private String bio;
    private Role role;
    private Status status;
    private float ratingAverage;
    private String documents;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {}

    // Constructeur inscription
    public User(String firstName, String lastName, String email,
                String password, String phone, String role) {
        this.firstName     = firstName;
        this.lastName      = lastName;
        this.email         = email;
        this.password      = password;
        this.phone         = phone;
        this.role          = Role.valueOf(role);
        this.status        = Status.PENDING_EMAIL;
        this.ratingAverage = 0;
        this.createdAt     = LocalDateTime.now();
        this.updatedAt     = LocalDateTime.now();
    }

    // Constructeur complet BDD
    public User(int id, String firstName, String lastName, String email,
                String password, String phone, String address, String city,
                String postalCode, String profilePhoto, String bio,
                String role, String status, float ratingAverage,
                String documents, LocalDateTime emailVerifiedAt,
                LocalDateTime lastLoginAt, LocalDateTime createdAt,
                LocalDateTime updatedAt) {
        this.id              = id;
        this.firstName       = firstName;
        this.lastName        = lastName;
        this.email           = email;
        this.password        = password;
        this.phone           = phone;
        this.address         = address;
        this.city            = city;
        this.postalCode      = postalCode;
        this.profilePhoto    = profilePhoto;
        this.bio             = bio;
        this.role            = Role.valueOf(role);
        this.status          = Status.valueOf(status);
        this.ratingAverage   = ratingAverage;
        this.documents       = documents;
        this.emailVerifiedAt = emailVerifiedAt;
        this.lastLoginAt     = lastLoginAt;
        this.createdAt       = createdAt;
        this.updatedAt       = updatedAt;
    }

    // Méthodes métier
    public boolean isActive()  { return this.status == Status.APPROVED; }
    public boolean isPending() {
        return this.status == Status.PENDING_EMAIL
                || this.status == Status.PENDING_ADMIN;
    }

    public boolean verifyEmail() {
        if (this.status == Status.PENDING_EMAIL) {
            this.emailVerifiedAt = LocalDateTime.now();
            this.status          = Status.PENDING_ADMIN;
            this.updatedAt       = LocalDateTime.now();
            return true;
        }
        return false;
    }

    public void approve()  { this.status = Status.APPROVED;  this.updatedAt = LocalDateTime.now(); }
    public void reject()   { this.status = Status.REJECTED;  this.updatedAt = LocalDateTime.now(); }
    public void suspend()  { this.status = Status.SUSPENDED; this.updatedAt = LocalDateTime.now(); }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt   = LocalDateTime.now();
    }

    public boolean hasRole(Role role) { return this.role == role; }
    public String getFullName()       { return this.firstName + " " + this.lastName; }

    // Getters & Setters
    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }
    public String getFirstName()                { return firstName; }
    public void setFirstName(String firstName)  { this.firstName = firstName; }
    public String getLastName()                 { return lastName; }
    public void setLastName(String lastName)    { this.lastName = lastName; }
    public String getEmail()                    { return email; }
    public void setEmail(String email)          { this.email = email; }
    public String getPassword()                 { return password; }
    public void setPassword(String password)    { this.password = password; }
    public String getPhone()                    { return phone; }
    public void setPhone(String phone)          { this.phone = phone; }
    public String getAddress()                  { return address; }
    public void setAddress(String address)      { this.address = address; }
    public String getCity()                     { return city; }
    public void setCity(String city)            { this.city = city; }
    public String getPostalCode()               { return postalCode; }
    public void setPostalCode(String p)         { this.postalCode = p; }
    public String getProfilePhoto()             { return profilePhoto; }
    public void setProfilePhoto(String p)       { this.profilePhoto = p; }
    public String getBio()                      { return bio; }
    public void setBio(String bio)              { this.bio = bio; }
    public Role getRole()                       { return role; }
    public void setRole(Role role)              { this.role = role; }
    public void setRole(String role)            { this.role = Role.valueOf(role); }
    public Status getStatus()                   { return status; }
    public void setStatus(Status status)        { this.status = status; }
    public void setStatus(String status)        { this.status = Status.valueOf(status); }
    public float getRatingAverage()             { return ratingAverage; }
    public void setRatingAverage(float r)       { this.ratingAverage = r; }
    public String getDocuments()                { return documents; }
    public void setDocuments(String documents)  { this.documents = documents; }
    public LocalDateTime getEmailVerifiedAt()   { return emailVerifiedAt; }
    public void setEmailVerifiedAt(LocalDateTime v) { this.emailVerifiedAt = v; }
    public LocalDateTime getLastLoginAt()       { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime v) { this.lastLoginAt = v; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime v)   { this.createdAt = v; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)   { this.updatedAt = v; }

    @Override
    public String toString() {
        return "User{" +
                "id="              + id            +
                ", fullName='"     + getFullName() + '\'' +
                ", email='"        + email         + '\'' +
                ", phone='"        + phone         + '\'' +
                ", city='"         + city          + '\'' +
                ", role="          + role          +
                ", status="        + status        +
                ", ratingAverage=" + ratingAverage +
                ", createdAt="     + createdAt     +
                '}';
    }
}