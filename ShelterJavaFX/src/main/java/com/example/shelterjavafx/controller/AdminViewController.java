package com.example.shelterjavafx.controller;

import com.example.shelterjavafx.model.Animal;
import com.example.shelterjavafx.model.AnimalCondition;
import com.example.shelterjavafx.model.AnimalShelter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.example.shelterjavafx.model.AnimalCondition.*;

public class AdminViewController {

    @FXML
    private Button backButton;
    @FXML
    private Button filterButton;
    @FXML
    private TableView<AnimalShelter> shelterTable;
    @FXML
    private TableColumn<AnimalShelter, String> shelterNameColumn;
    @FXML
    private TableColumn<AnimalShelter, Integer> maxCapacityColumn;
    @FXML
    private TableColumn<AnimalShelter, Integer> currentAnimalsColumn;

    @FXML
    private TableView<Animal> animalTable;
    @FXML
    private TableColumn<Animal, String> animalNameColumn;
    @FXML
    private TableColumn<Animal, String> animalSpeciesColumn;
    @FXML
    private TableColumn<Animal, AnimalCondition> animalConditionColumn;
    @FXML
    private TableColumn<Animal, Integer> animalAgeColumn;
    @FXML
    private TableColumn<Animal, Integer> animalPriceColumn;

    @FXML
    private TextField shelterFilterTextField;
    @FXML
    private TextField animalFilterTextField;

    @FXML
    private ComboBox<AnimalCondition> stateComboBox;

    @FXML
    private void applyShelterFilters() {
        String filterText = shelterFilterTextField.getText().toLowerCase().trim();
        ObservableList<AnimalShelter> filteredShelters = FXCollections.observableArrayList();

        for (AnimalShelter shelter : shelters) {
            if (filterText.isEmpty() || shelter.getShelterName().toLowerCase().contains(filterText)) {
                filteredShelters.add(shelter);
            }
        }

        shelterTable.setItems(filteredShelters);
    }

    @FXML
    private void applyAnimalFilters() {
        String filterText = animalFilterTextField.getText().toLowerCase().trim();
        ObservableList<Animal> filteredAnimals = FXCollections.observableArrayList();
        AnimalShelter selectedShelter = shelterTable.getSelectionModel().getSelectedItem();

        if (selectedShelter != null) {
            List<Animal> animals = selectedShelter.getAnimals();

            for (Animal animal : animals) {
                boolean matchesNameOrSpecies = filterText.isEmpty() || animal.getName().toLowerCase().contains(filterText) || animal.getSpecies().toLowerCase().contains(filterText);

                if (matchesNameOrSpecies) {
                    filteredAnimals.add(animal);
                }
            }
        }

        animalTable.setItems(filteredAnimals);
    }

    @FXML
    private void applyAnimalFiltersByCondition() {
        AnimalCondition selectedCondition = stateComboBox.getValue();
        ObservableList<Animal> filteredAnimals = FXCollections.observableArrayList();
        AnimalShelter selectedShelter = shelterTable.getSelectionModel().getSelectedItem();

        if (selectedShelter != null) {
            List<Animal> animals = selectedShelter.getAnimals();

            for (Animal animal : animals) {
                boolean matchesCondition = selectedCondition == null || animal.getCondition() == selectedCondition;

                if (matchesCondition) {
                    filteredAnimals.add(animal);
                }
            }
        }
        animalTable.setItems(filteredAnimals);
    }

    @FXML
    private void handleBackButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/shelterjavafx/view/login-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddShelterButtonClick(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Shelter");
        dialog.setHeaderText("Enter Shelter Details:");
        TextField shelterNameField = new TextField();
        shelterNameField.setPromptText("Shelter Name");
        TextField shelterCapacityField = new TextField();
        shelterCapacityField.setPromptText("Maximum Capacity");

        VBox vbox = new VBox(10, shelterNameField, shelterCapacityField);
        dialog.getDialogPane().setContent(vbox);

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == okButton) {
            String shelterName = shelterNameField.getText();
            String capacityInput = shelterCapacityField.getText();

            if (shelterName != null && !shelterName.isEmpty() && capacityInput != null && !capacityInput.isEmpty()) {
                try {
                    boolean shelterExists = shelters.stream().anyMatch(shelter -> shelter.getShelterName().equalsIgnoreCase(shelterName));

                    if (shelterExists) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Shelter Already Exists");
                        alert.setContentText("A shelter with this name already exists. Please choose a different name.");
                        alert.showAndWait();
                    } else {
                        int maxCapacity = Integer.parseInt(capacityInput);
                        AnimalShelter newShelter = new AnimalShelter(shelterName, maxCapacity);
                        shelters.add(newShelter);
                        shelterTable.getItems().setAll(shelters);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid capacity input.");
                }
            }
        }
    }

    @FXML
    private void handleAddAnimalButtonClick(ActionEvent event) {
        AnimalShelter selectedShelter = shelterTable.getSelectionModel().getSelectedItem();
        if (selectedShelter != null) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Add Animal");
            dialog.setHeaderText("Enter Animal Details:");

            TextField animalNameField = new TextField();
            animalNameField.setPromptText("Animal Name");

            TextField speciesField = new TextField();
            speciesField.setPromptText("Animal Species");

            TextField ageField = new TextField();
            ageField.setPromptText("Animal Age");

            TextField priceField = new TextField();
            priceField.setPromptText("Animal Price");

            ChoiceBox<AnimalCondition> conditionChoiceBox = new ChoiceBox<>();
            conditionChoiceBox.getItems().addAll(AnimalCondition.values());
            conditionChoiceBox.setValue(AnimalCondition.HEALTHY);

            VBox vbox = new VBox(10, animalNameField, speciesField, ageField, priceField, conditionChoiceBox);
            dialog.getDialogPane().setContent(vbox);

            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == okButton) {
                String animalName = animalNameField.getText();
                String animalSpecies = speciesField.getText();
                String ageInput = ageField.getText();
                String priceInput = priceField.getText();
                AnimalCondition animalCondition = conditionChoiceBox.getValue();

                if (animalName != null && !animalName.isEmpty() && animalSpecies != null && !animalSpecies.isEmpty() && ageInput != null && !ageInput.isEmpty() && priceInput != null && !priceInput.isEmpty()) {
                    try {
                        int age = Integer.parseInt(ageInput);
                        double price = Double.parseDouble(priceInput);
                        Animal newAnimal = new Animal(animalName, animalSpecies, animalCondition, age, price);
                        boolean animalExists = selectedShelter.getAnimals().stream().anyMatch(existingAnimal -> existingAnimal.compareTo(newAnimal) == 0);

                        if (animalExists) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Animal Already Exists");
                            alert.setContentText("An animal with the same name, species, and age already exists in this shelter.");
                            alert.showAndWait();
                        } else {
                            selectedShelter.addAnimal(newAnimal);
                            updateAnimalTable(selectedShelter);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid age or price input.");
                    }
                }
            }
        } else {
            System.out.println("No shelter selected.");
        }
    }

    @FXML
    private void handleRemoveShelterButtonClick(ActionEvent event) {
        AnimalShelter selectedShelter = shelterTable.getSelectionModel().getSelectedItem();

        if (selectedShelter != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Remove Shelter");
            alert.setHeaderText("Are you sure you want to remove this shelter?");
            alert.setContentText("This action cannot be undone.");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                shelters.remove(selectedShelter);
                shelterTable.getItems().setAll(shelters);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Shelter Selected");
            alert.setHeaderText("Please select a shelter to remove.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleRemoveAnimalButtonClick(ActionEvent event) {
        AnimalShelter selectedShelter = shelterTable.getSelectionModel().getSelectedItem();
        Animal selectedAnimal = animalTable.getSelectionModel().getSelectedItem();

        if (selectedShelter != null && selectedAnimal != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Remove Animal");
            alert.setHeaderText("Are you sure you want to remove this animal?");
            alert.setContentText("This action cannot be undone.");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                selectedShelter.removeAnimal(selectedAnimal);
                updateAnimalTable(selectedShelter);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Animal Selected");
            alert.setHeaderText("Please select an animal to remove.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleEditShelterButtonClick(ActionEvent event) {
        AnimalShelter selectedShelter = shelterTable.getSelectionModel().getSelectedItem();

        if (selectedShelter != null) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Shelter");
            dialog.setHeaderText("Edit Shelter Details:");

            TextField shelterNameField = new TextField(selectedShelter.getShelterName());
            shelterNameField.setPromptText("Shelter Name");

            TextField shelterCapacityField = new TextField(String.valueOf(selectedShelter.getMaxCapacity()));
            shelterCapacityField.setPromptText("Maximum Capacity");

            VBox vbox = new VBox(10, shelterNameField, shelterCapacityField);
            dialog.getDialogPane().setContent(vbox);

            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == okButton) {
                String newShelterName = shelterNameField.getText();
                String newCapacityInput = shelterCapacityField.getText();

                if (!newShelterName.isEmpty() && !newCapacityInput.isEmpty()) {
                    try {
                        int newCapacity = Integer.parseInt(newCapacityInput);

                        for (AnimalShelter shelter : shelters) {
                            if (shelter.getShelterName().equals(newShelterName) && shelter != selectedShelter) {
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setTitle("Shelter Already Exists");
                                alert.setHeaderText("A shelter with this name already exists.");
                                alert.showAndWait();
                                return;
                            }
                        }

                        selectedShelter.setShelterName(newShelterName);
                        selectedShelter.setMaxCapacity(newCapacity);
                        shelterTable.refresh();  // Odświeżamy tabelę schronisk
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid capacity input.");
                    }
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Shelter Selected");
            alert.setHeaderText("Please select a shelter to edit.");
            alert.showAndWait();
        }
    }


    @FXML
    private void handleEditAnimalButtonClick(ActionEvent event) {
        AnimalShelter selectedShelter = shelterTable.getSelectionModel().getSelectedItem();
        Animal selectedAnimal = animalTable.getSelectionModel().getSelectedItem();

        if (selectedShelter != null && selectedAnimal != null) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Animal");
            dialog.setHeaderText("Edit Animal Details:");

            TextField animalNameField = new TextField(selectedAnimal.getName());
            animalNameField.setPromptText("Animal Name");

            TextField speciesField = new TextField(selectedAnimal.getSpecies());
            speciesField.setPromptText("Animal Species");

            TextField ageField = new TextField(String.valueOf(selectedAnimal.getAge()));
            ageField.setPromptText("Animal Age");

            TextField priceField = new TextField(String.valueOf(selectedAnimal.getPrice()));
            priceField.setPromptText("Animal Price");

            ComboBox<AnimalCondition> conditionComboBox = new ComboBox<>();
            conditionComboBox.getItems().setAll(AnimalCondition.values());
            conditionComboBox.setValue(selectedAnimal.getCondition());

            VBox vbox = new VBox(10, animalNameField, speciesField, ageField, priceField, conditionComboBox);
            dialog.getDialogPane().setContent(vbox);

            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == okButton) {
                String newAnimalName = animalNameField.getText();
                String newSpecies = speciesField.getText();
                String newAgeInput = ageField.getText();
                String newPriceInput = priceField.getText();

                if (!newAnimalName.isEmpty() && !newSpecies.isEmpty() && !newAgeInput.isEmpty() && !newPriceInput.isEmpty()) {
                    try {
                        int newAge = Integer.parseInt(newAgeInput);
                        double newPrice = Double.parseDouble(newPriceInput);

                        Animal newAnimal = new Animal(newAnimalName, newSpecies, conditionComboBox.getValue(), newAge, newPrice);
                        for (Animal animal : selectedShelter.getAnimals()) {
                            if (animal.compareTo(newAnimal) == 0 && animal != selectedAnimal) {
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setTitle("Animal Already Exists");
                                alert.setHeaderText("An animal with these details already exists.");
                                alert.showAndWait();
                                return;
                            }
                        }

                        selectedAnimal.setName(newAnimalName);
                        selectedAnimal.setSpecies(newSpecies);
                        selectedAnimal.setAge(newAge);
                        selectedAnimal.setPrice(newPrice);
                        selectedAnimal.setCondition(conditionComboBox.getValue());

                        animalTable.refresh();
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input.");
                    }
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Animal Selected");
            alert.setHeaderText("Please select an animal to edit.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleSortSheltersByMaxCapacity(ActionEvent event) {
        ObservableList<AnimalShelter> shelters = shelterTable.getItems();
        FXCollections.sort(shelters, Comparator.comparingInt(AnimalShelter::getMaxCapacity).reversed());
    }

    @FXML
    private void handleMouseClickOutsideTextField(MouseEvent event) {
        shelterFilterTextField.getScene().getRoot().requestFocus();
        animalFilterTextField.getScene().getRoot().requestFocus();
    }

    private List<AnimalShelter> shelters = new ArrayList<>();

    public void initialize() {
        shelterFilterTextField.setOnAction(event -> applyShelterFilters());
        animalFilterTextField.setOnAction(event -> applyAnimalFilters());

        stateComboBox.setItems(FXCollections.observableArrayList(AnimalCondition.values()));
        stateComboBox.setValue(null);
        AnimalShelter shelterA = new AnimalShelter("Shelter A", 50);
        shelterA.addAnimal(new Animal("Dog", "Bulldog", HEALTHY, 3, 122.12));
        shelterA.addAnimal(new Animal("Cat", "Siamese", SICK, 9, 12222));

        AnimalShelter shelterB = new AnimalShelter("Shelter B", 30);
        shelterB.addAnimal(new Animal("Dog", "Labrador", ADOPTION, 12, 9000));

        shelters.add(shelterA);
        shelters.add(shelterB);

        shelterNameColumn.setCellValueFactory(new PropertyValueFactory<>("shelterName"));
        maxCapacityColumn.setCellValueFactory(new PropertyValueFactory<>("maxCapacity"));
        currentAnimalsColumn.setCellValueFactory(new PropertyValueFactory<>("currentAnimals"));

        shelterTable.getItems().setAll(shelters);

        animalNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        animalSpeciesColumn.setCellValueFactory(new PropertyValueFactory<>("species"));
        animalConditionColumn.setCellValueFactory(new PropertyValueFactory<>("condition"));
        animalAgeColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        animalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        shelterTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateAnimalTable(newValue);
            }
        });
    }

    private void updateAnimalTable(AnimalShelter shelter) {
        animalTable.getItems().setAll(shelter.getAnimals());
    }
}