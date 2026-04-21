package main;

import entities.Animal;
import services.AnimalService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TestAnimalCRUD {
    public static void main(String[] args) {
        AnimalService animalService = new AnimalService();

        try {
            // 1) Add a new animal
            Animal animal = new Animal(
                    1,                          // owner_id (use an existing user ID from your database)
                    "Max",                      // name
                    "Dog",                      // species
                    "Golden Retriever",         // breed
                    LocalDate.of(2020, 5, 15), // birth_date
                    "Male",                     // gender
                    30.5f,                      // weight
                    "Golden",                   // color
                    true,                       // is_neutered
                    "CHIP123456789",           // microchip_number
                    "max_photo.jpg"            // photo
            );
            animalService.add(animal);
            System.out.println("Added: " + animal);

            // 2) Get all animals
            List<Animal> allAnimals = animalService.getAll();
            System.out.println("\nAll animals:");
            allAnimals.forEach(System.out::println);

            // 3) Get by ID
            Animal found = animalService.getById(animal.getId());
            System.out.println("\nFound by ID: " + found);

            // 4) Get animals by owner
            List<Animal> ownerAnimals = animalService.getByOwnerId(1);
            System.out.println("\nAnimals owned by user 1:");
            ownerAnimals.forEach(System.out::println);

            // 5) Get animals by status
            List<Animal> pendingAnimals = animalService.getByStatus("PENDING");
            System.out.println("\nPending animals:");
            pendingAnimals.forEach(System.out::println);

            // 6) Get animals by species
            List<Animal> dogs = animalService.getBySpecies("Dog");
            System.out.println("\nDogs:");
            dogs.forEach(System.out::println);

            // 7) Update animal
            if (found != null) {
                found.setWeight(32.0f);
                found.setColor("Light Golden");
                found.setAvailabilityStatus("AVAILABLE");
                animalService.update(found);
                System.out.println("\nUpdated: " + animalService.getById(found.getId()));
            }

            // 8) Delete (uncomment if needed)
            // animalService.delete(animal.getId());
            // System.out.println("Deleted.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}