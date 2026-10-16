import java.util.Scanner;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

interface BillOperations {
    void displayCategories();
    void showMenu(int type);
    void addOrder(int selectedItem, int qty);
    void printFinalBill(String customerName, String enteredCoupon);
}

class MenuItem {
    private int code;
    private String name;
    private double price;
    private int qty;

    public MenuItem() {
        this.code = 0;
        this.name = "Unknown";
        this.price = 0.0;
        this.qty = 0;
    }

    public MenuItem(int code, String name, double price) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.qty = 0;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQty() {
        return qty;
    }

    public void addQty(int q) {
        this.qty = this.qty + q;
    }

    @Override
    public String toString() {
        return code + ". " + name + " = Rs " + price;
    }
}

class BaseRestaurant {
    public static final String RESTAURANT_NAME = "KLH Spice Garden Restaurant";
    public static final double SERVICE_TAX_RATE = 0.05;
    public static final double GST_RATE = 0.18;
    public static final String PROMO_COUPON = "Enjoy100G6";

    public static void displayWelcomeBanner() {
        System.out.println("===========================================");
        System.out.println("   WELCOME TO " + RESTAURANT_NAME.toUpperCase());
        System.out.println("===========================================");
    }
}

public class RestaurantBillingSystem extends BaseRestaurant implements BillOperations {

    int[] item_code = {301, 101, 102, 103, 104, 105, 201, 202, 203, 204};
    String[] item_name = {
        "Egg Bhurji", "Chicken Biryani", "Chicken Noodles", "Mutton Biryani",
        "Chicken Lolipop", "Chicken Tandori", "Paneer Tikka", "Methi Chaman",
        "Palak Paneer", "Paneer Butter Masala"
    };
    double[] item_price = {110.0, 220.0, 120.0, 240.0, 160.0, 240.0, 120.0, 240.0, 160.0, 210.0};
    int[] item_qty = new int[10];

    MenuItem[] menu_items = new MenuItem[10];
    int[] sorted_codes = new int[10];
    double total_amount = 0.0;

    public RestaurantBillingSystem() {
        int i = 0;
        while (i < item_code.length) {
            menu_items[i] = new MenuItem(item_code[i], item_name[i], item_price[i]);
            sorted_codes[i] = item_code[i];
            i++;
        }
        Arrays.sort(sorted_codes);
    }

    @Override
    public void displayCategories() {
        System.out.println("=== MENU CATEGORIES ===");
        System.out.println("1. Non-Veg");
        System.out.println("2. Veg");
        System.out.println("3. Eggitarian");
        System.out.println("4. All");
    }

    @Override
    public void showMenu(int type) {
        int i;
        switch (type) {
            case 1:
                i = 1;
                while (i <= 5) {
                    System.out.println(menu_items[i].toString());
                    i++;
                }
                break;
            case 2:
                i = 6;
                while (i <= 9) {
                    System.out.println(menu_items[i].toString());
                    i++;
                }
                break;
            case 3:
                System.out.println(menu_items[0].toString());
                break;
            case 4:
                i = 0;
                while (i <= 9) {
                    System.out.println(menu_items[i].toString());
                    i++;
                }
                break;
            default:
                System.out.println("Wrong Type Selected. Showing All Items:");
                i = 0;
                while (i <= 9) {
                    System.out.println(menu_items[i].toString());
                    i++;
                }
        }
    }

    public void addOrder(int selectedItem) {
        addOrder(selectedItem, 1);
    }

    public void addOrder(int selectedItem, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        int binaryCheck = Arrays.binarySearch(sorted_codes, selectedItem);
        if (binaryCheck < 0) {
            System.out.println("Invalid Code. Please try again.");
            return;
        }

        int i = 0;
        boolean found = false;
        while (i < item_code.length && !found) {
            if (item_code[i] == selectedItem) {
                found = true;
            } else {
                i++;
            }
        }

        if (found) {
            item_qty[i] += qty;
            menu_items[i].addQty(qty);
            total_amount += (item_price[i] * qty);
            System.out.println(qty + " x " + item_name[i] + " added. Subtotal: Rs " + total_amount);
        } else {
            System.out.println("Invalid Code. Please try again.");
        }
    }

    @Override
    public void printFinalBill(String customerName, String enteredCoupon) {
        if (total_amount > 0) {
            double discount = 0.0;
            String cleanCoupon = enteredCoupon.trim();

            if (cleanCoupon.equalsIgnoreCase(PROMO_COUPON)) {
                discount = 50.0;
                if (discount > total_amount) {
                    discount = total_amount;
                }
            }

            double taxable_amount = total_amount - discount;
            double service_tax = taxable_amount * SERVICE_TAX_RATE;
            double gst = taxable_amount * GST_RATE;
            double final_amount = taxable_amount + service_tax + gst;

            StringBuilder receipt = new StringBuilder();
            receipt.append("=============== FINAL BILL ===============\n");
            receipt.append("Customer Name: " + customerName.trim().toUpperCase() + "\n");
            receipt.append("------------------------------------------\n");
            receipt.append("Item                  Qty    Price    Total\n");

            int i = 0;
            while (i < item_code.length) {
                if (item_qty[i] > 0) {
                    double lineTotal = item_qty[i] * item_price[i];
                    receipt.append(item_name[i] + "    " + item_qty[i] + "     " + item_price[i] + "     " + lineTotal + "\n");
                }
                i++;
            }

            receipt.append("------------------------------------------\n");
            receipt.append("Subtotal:            Rs " + total_amount + "\n");
            if (discount > 0) {
                receipt.append("Coupon Discount:    -Rs " + discount + " (" + PROMO_COUPON + ")\n");
            }
            receipt.append("Service Tax (5%):    Rs " + service_tax + "\n");
            receipt.append("GST (18%):           Rs " + gst + "\n");
            receipt.append("------------------------------------------\n");
            receipt.append("Total Due:           Rs " + final_amount + "\n");
            receipt.append("You Have earned a Coupon Worth 100 In Movie Ticket From Group6 Theater\n");
            receipt.append("Coupon = " + PROMO_COUPON + "\n");
            receipt.append("Contact Group 6 For Further Info\n");
            receipt.append("==========================================\n");

            System.out.println(receipt.toString());

            Path billPath = Paths.get("Final_Bill_Receipt.txt");
            try (BufferedWriter writer = Files.newBufferedWriter(billPath)) {
                writer.write(receipt.toString());
                System.out.println("Receipt saved to file: " + billPath.toString());
            } catch (IOException e) {
                System.out.println("File Save Error: " + e.getMessage());
            }

            try (BufferedReader reader = Files.newBufferedReader(billPath)) {
                String firstLine = reader.readLine();
                System.out.println("Verified File Header: " + firstLine);
            } catch (IOException e) {
                System.out.println("File Read Verification Error: " + e.getMessage());
            }
        } else {
            System.out.println("No items were ordered.");
        }
    }

    public static void main(String[] args) {
        RestaurantBillingSystem billing = new RestaurantBillingSystem();
        Scanner sc = new Scanner(System.in);

        displayWelcomeBanner();

        System.out.print("Enter Customer Name: ");
        String customerName = sc.nextLine();
        if (customerName.trim().length() == 0) {
            customerName = "Guest";
        }

        billing.displayCategories();

        int type = 4;
        try {
            System.out.print("Enter The Type (1-4): ");
            type = sc.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Invalid category input! Defaulting to All Categories (4).");
            sc.nextLine();
            type = 4;
        }

        System.out.println("=======================");
        System.out.println("          MENU         ");
        System.out.println("=======================");
        billing.showMenu(type);
        System.out.println("============================");

        boolean ordering = true;
        while (ordering) {
            try {
                System.out.print("Enter Item Code (or 0 to Generate Bill): ");
                int selectedItem = sc.nextInt();

                if (selectedItem == 0) {
                    ordering = false;
                    continue;
                }

                System.out.print("Enter the quantity: ");
                int qty = sc.nextInt();

                billing.addOrder(selectedItem, qty);
            } catch (InputMismatchException e) {
                System.out.println("Error: Please enter valid numeric digits only.");
                sc.nextLine();
            } catch (IllegalArgumentException e) {
                System.out.println("Validation Error: " + e.getMessage());
            } finally {
            }
        }

        sc.nextLine(); 
        System.out.print("Enter Coupon Code (or press Enter to skip): ");
        String enteredCoupon = sc.nextLine();

        billing.printFinalBill(customerName, enteredCoupon);
        sc.close();
    }
}
