package entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Animal {

    public enum AvailabilityStatus {
        AVAILABLE, PENDING, ADOPTED, PUBLISHED, REJECTED
    }

    private int id;
    private int ownerId;
    private String name;
    private String species;
    private String breed;
    private LocalDate birthDate;
    private String gender;
    private float weight;
    private String color;
    private AvailabilityStatus availabilityStatus;
    private boolean isNeutered;
    private String microchipNumber;
    private String photo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Empty constructor
    public Animal() {}

    // Constructor for adding new animal
    public Animal(int ownerId, String name, String species, String breed,
                  LocalDate birthDate, String gender, float weight,
                  String color, boolean isNeutered, String microchipNumber, String photo) {
        this.ownerId = ownerId;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.birthDate = birthDate;
        this.gender = gender;
        this.weight = weight;
        this.color = color;
        this.isNeutered = isNeutered;
        this.microchipNumber = microchipNumber;
        this.photo = photo;
        this.availabilityStatus = AvailabilityStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Full constructor (from database)
    public Animal(int id, int ownerId, String name, String species, String breed,
                  LocalDate birthDate, String gender, float weight, String color,
                  String availabilityStatus, boolean isNeutered,
                  String microchipNumber, String photo,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.birthDate = birthDate;
        this.gender = gender;
        this.weight = weight;
        this.color = color;
        this.availabilityStatus = AvailabilityStatus.valueOf(availabilityStatus);
        this.isNeutered = isNeutered;
        this.microchipNumber = microchipNumber;
        this.photo = photo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Business methods
    public int getAge() {
        if (birthDate == null) return 0;
        return LocalDate.now().getYear() - birthDate.getYear();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public AvailabilityStatus getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(AvailabilityStatus status) { this.availabilityStatus = status; }
    public void setAvailabilityStatus(String status) { this.availabilityStatus = AvailabilityStatus.valueOf(status); }

    public boolean isNeutered() { return isNeutered; }
    public void setNeutered(boolean neutered) { isNeutered = neutered; }

    public String getMicrochipNumber() { return microchipNumber; }
    public void setMicrochipNumber(String microchipNumber) { this.microchipNumber = microchipNumber; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Animal{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", species='" + species + '\'' +
                ", breed='" + breed + '\'' +
                ", age=" + getAge() +
                ", gender='" + gender + '\'' +
                ", status=" + availabilityStatus +
                '}';
    }
}