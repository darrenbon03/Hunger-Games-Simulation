package games;

import java.util.ArrayList;

/**
 * This class contains methods to represent the Hunger Games using BSTs.
 * Moves people from input files to districts, eliminates people from the game,
 * and determines a possible winner.
 * 
 * @author Pranay Roni
 * @author Maksims Kurjanovics Kravcenko
 * @author Kal Pandit
 */
public class HungerGames {

    private ArrayList<District> districts;  // all districts in Panem.
    private TreeNode            game;       // root of the BST. The BST contains districts that are still in the game.

    /**
     * ***** DO NOT REMOVE OR UPDATE this method *********
     * Default constructor, initializes a list of districts.
     */
    public HungerGames() {
        districts = new ArrayList<>();
        game = null;
        StdRandom.setSeed(2023);
    }

    /**
     * ***** DO NOT REMOVE OR UPDATE this method *********
     * Sets up Panem, the universe in which the Hunger Games takes place.
     * Reads districts and people from the input file.
     * 
     * @param filename will be provided by client to read from using StdIn
     */
    public void setupPanem(String filename) { 
        StdIn.setFile(filename);
        setupDistricts(filename); 
        setupPeople(filename);
    }

    /**
     * Reads the following from input file:
     * - Number of districts
     * - District ID's (insert in order of insertion)
     * Insert districts into the districts ArrayList in order of appearance.
     * 
     * @param filename will be provided by client to read from using StdIn
     */
    public void setupDistricts (String filename) {

        StdIn.setFile(filename);
        int numberOfDistricts = StdIn.readInt();
        
        for (int i = 0; i < numberOfDistricts; i++) {
            int districtId = StdIn.readInt();
            District newDistrict = new District(districtId);
            districts.add(newDistrict);
        }
    }

    /**
     * Reads the following from input file (continues to read from the SAME input file as setupDistricts()):
     * Number of people
     * Space-separated: first name, last name, birth month (1-12), age, district id, effectiveness
     * Districts will be initialized to the instance variable districts
     * 
     * Persons will be added to corresponding district in districts defined by districtID
     * 
     * @param filename will be provided by client to read from using StdIn
     */
    public void setupPeople (String filename) {
        int numberOfPeople = StdIn.readInt();

        for (int i = 0; i < numberOfPeople; i++) {
            String firstName = StdIn.readString();
            String lastName = StdIn.readString();
            int birthMonth = StdIn.readInt();
            int age = StdIn.readInt();
            int districtID = StdIn.readInt();
            int effectiveness = StdIn.readInt();
    
            Person newPerson = new Person(birthMonth, firstName, lastName, age, districtID, effectiveness);
            if (age >= 12 && age < 18) {
                newPerson.setTessera(true);
            }
            for (District district : districts) {
                if (district.getDistrictID() == districtID) {
                    if (birthMonth % 2 == 0) {
                        district.addEvenPerson(newPerson);
                    } else {
                        district.addOddPerson(newPerson);
                    }
                    break;
                }
            }
        }
    
    
    }

    /**
     * Adds a district to the game BST.
     * If the district is already added, do nothing
     * 
     * @param root        the TreeNode root which we access all the added districts
     * @param newDistrict the district we wish to add
     */
    public void addDistrictToGame(TreeNode root, District newDistrict) {
        TreeNode newNode = new TreeNode(newDistrict, null, null);

    if (this.game == null) {
        this.game = newNode;
    } else {
        insertNode(this.game, newNode);
    }
    //if (districts != null) {
        districts.remove(newDistrict);
    //}
}

private void insertNode(TreeNode current, TreeNode newNode) {
    District newDistrict = newNode.getDistrict();

    if (newDistrict.getDistrictID() < current.getDistrict().getDistrictID()) {
        if (current.getLeft() == null) {
            current.setLeft(newNode);
        } else {
            insertNode(current.getLeft(), newNode);
        }
    } else if (newDistrict.getDistrictID() > current.getDistrict().getDistrictID()) {
        if (current.getRight() == null) {
            current.setRight(newNode);
        } else {
            insertNode(current.getRight(), newNode);
        }
    }

    }
    

    /**
     * Searches for a district inside of the BST given the district id.
     * 
     * @param id the district to search
     * @return the district if found, null if not found
     */
    public District findDistrict(int id) {
        return findDistrictRecursive(this.getRoot(), id);
}

private District findDistrictRecursive(TreeNode node, int id) {
    if (node == null) {
        return null; 
    }

    District currentDistrict = node.getDistrict();
    if (currentDistrict.getDistrictID() == id) {
        return currentDistrict;
    } else if (id < currentDistrict.getDistrictID()) {
        return findDistrictRecursive(node.getLeft(), id);
    } else {
        return findDistrictRecursive(node.getRight(), id);
    }
    }
    
    

    /**
     * Selects two duelers from the tree, following these rules:
     * - One odd person and one even person should be in the pair.
     * - Dueler with Tessera (age 12-18, use tessera instance variable) must be
     * retrieved first.
     * - Find the first odd person and even person (separately) with Tessera if they
     * exist.
     * - If you can't find a person, use StdRandom.uniform(x) where x is the respective 
     * population size to obtain a dueler.
     * - Add odd person dueler to person1 of new DuelerPair and even person dueler to
     * person2.
     * - People from the same district cannot fight against each other.
     * 
     * @return the pair of dueler retrieved from this method.
     */
    public DuelPair selectDuelers() {
    Person oddDueler = null;
    Person evenDueler = null;

    // First pass: try to find an odd person with tessera and an even person with tessera from different districts
    oddDueler = selectDuelerRecursive(this.game, true, true, -1);
    evenDueler = selectDuelerRecursive(this.game, true, false, (oddDueler != null) ? oddDueler.getDistrictID() : -1);

    // Second pass, if necessary: select randomly for any missing duelers ensuring they are from different districts
    if (oddDueler == null) {
        oddDueler = selectDuelerRecursive(this.game, false, true, (evenDueler != null) ? evenDueler.getDistrictID() : -1);
    }
    if (evenDueler == null) {
        evenDueler = selectDuelerRecursive(this.game, false, false, (oddDueler != null) ? oddDueler.getDistrictID() : -1);
    }

    // Return the pair of duelers
    return new DuelPair(oddDueler, evenDueler);
}

private Person selectDuelerRecursive(TreeNode node, boolean searchForTessera, boolean searchForOdd, int excludeDistrictId) { {
    if (node == null) {
        return null; // Base case: reached the end of a branch
    }

    District district = node.getDistrict();
    Person selectedPerson = null;

    // Skip this district if it's the one to exclude
    if (district.getDistrictID() != excludeDistrictId) {
        selectedPerson = findDueler(
            searchForOdd ? district.getOddPopulation() : district.getEvenPopulation(),
            searchForTessera
        );
    }

    // If a person is found, return that person
    if (selectedPerson != null) {
        return selectedPerson;
    }

    // Search left
    Person leftPerson = selectDuelerRecursive(node.getLeft(), searchForTessera, searchForOdd, excludeDistrictId);
    if (leftPerson != null) {
        return leftPerson;
    }

    // Search right
    return selectDuelerRecursive(node.getRight(), searchForTessera, searchForOdd, excludeDistrictId);}
}

private Person findDueler(ArrayList<Person> population, boolean searchForTessera) {
    if (searchForTessera) {
        for (Person person : population) {
            if (person.getTessera()) {
                return person; // Return the first person with tessera
            }
        }
    } else if (!population.isEmpty()) {
        int randomIndex = StdRandom.uniform(population.size());
        return population.get(randomIndex); // Select randomly if not specifically looking for tessera
    }
    return null;
}

    /**
     * Deletes a district from the BST when they are eliminated from the game.
     * Districts are identified by id's.
     * If district does not exist, do nothing.
     * 
     * This is similar to the BST delete we have seen in class.
     * 
     * @param id the ID of the district to eliminate
     */
    public void eliminateDistrict(int id) {       
        game = eliminateDistrictRecursive(game, id);
        
       
}

private TreeNode eliminateDistrictRecursive(TreeNode root, int id) {
    if (root == null) {
        return null;
    }

    if (id < root.getDistrict().getDistrictID()) {
        root.setLeft(eliminateDistrictRecursive(root.getLeft(), id));
    } else if (id > root.getDistrict().getDistrictID()) {
        root.setRight(eliminateDistrictRecursive(root.getRight(), id));
    } else {
        // If the node has one child or no children
        if (root.getLeft() == null) {
            return root.getRight();
        } else if (root.getRight() == null) {
            return root.getLeft();
        }
        
        // If the node has two children
        // Find the inorder successor
        TreeNode successorParent = root;
        TreeNode successor = root.getRight();
        while (successor.getLeft() != null) {
            successorParent = successor;
            successor = successor.getLeft();
        }

        // If the inorder successor has a right child, attach it to the successor's parent
        if (successorParent != root) {
            successorParent.setLeft(successor.getRight());
        } else {
            successorParent.setRight(successor.getRight());
        }

        // Replace the root's district with the successor's district
        root.setDistrict(successor.getDistrict());

        // The successor node is now removed from the tree
    }
    return root;
    
}



    

    /**
     * Eliminates a dueler from a pair of duelers.
     * - Both duelers in the DuelPair argument given will duel
     * - Winner gets returned to their District
     * - Eliminate a District if it only contains a odd person population or even
     * person population
     * 
     * @param pair of persons to fight each other.
     */
    public void eliminateDueler(DuelPair pair) {
        Person person1 = pair.getPerson1();
        Person person2 = pair.getPerson2();
    
        // Handle the incomplete pair...
        // ...
    
        // Handle the complete pair...
        Person winner = person1.duel(person2);
        Person loser = (winner == person1) ? person2 : person1;
    
        // Remove the loser from their district's population in the BST
        removePersonFromDistrictInBST(loser);
    
        // Add the winner back to their district's population in the BST
        addPersonToDistrictInBST(winner);
    
        // Check if the loser's district needs to be eliminated
        checkAndEliminateDistrictInBST(loser.getDistrictID());
    
        // Check the winner's district if it's different from the loser's
        if (winner.getDistrictID() != loser.getDistrictID()) {
            checkAndEliminateDistrictInBST(winner.getDistrictID());
        }
    }
    
    private void removePersonFromDistrictInBST(Person person) {
        TreeNode districtNode = findDistrictNode(game, person.getDistrictID());
        if (districtNode != null) {
            District district = districtNode.getDistrict();
            ArrayList<Person> population = (person.getBirthMonth() % 2 == 0) ? 
                                           district.getEvenPopulation() : district.getOddPopulation();
            population.remove(person);
        }
    }
    
    private void addPersonToDistrictInBST(Person person) {
        TreeNode districtNode = findDistrictNode(game, person.getDistrictID());
        if (districtNode != null) {
            District district = districtNode.getDistrict();
            ArrayList<Person> population = (person.getBirthMonth() % 2 == 0) ? 
                                           district.getEvenPopulation() : district.getOddPopulation();
            if (!population.contains(person)) {
                population.add(person);
            }
        }
    }
    
    private void checkAndEliminateDistrictInBST(int districtID) {
        TreeNode districtNode = findDistrictNode(game, districtID);
        if (districtNode != null) {
            District district = districtNode.getDistrict();
            if (district.getOddPopulation().isEmpty() || district.getEvenPopulation().isEmpty()) {
                game = eliminateDistrictRecursive(game, districtID);
            }
        }
    }
    
    private TreeNode findDistrictNode(TreeNode current, int districtID) {
        if (current == null) {
            return null;
        }
        if (districtID == current.getDistrict().getDistrictID()) {
            return current;
        } else if (districtID < current.getDistrict().getDistrictID()) {
            return findDistrictNode(current.getLeft(), districtID);
        } else {
            return findDistrictNode(current.getRight(), districtID);
        }
    }
    
        



    

    /**
     * ***** DO NOT REMOVE OR UPDATE this method *********
     * 
     * Obtains the list of districts for the Driver.
     * 
     * @return the ArrayList of districts for selection
     */
    public ArrayList<District> getDistricts() {
        return this.districts;
    }

    /**
     * ***** DO NOT REMOVE OR UPDATE this method *********
     * 
     * Returns the root of the BST
     */
    public TreeNode getRoot() {
        return game;
    }
}
