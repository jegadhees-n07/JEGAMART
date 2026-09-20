package com.jdmart.config;

import com.jdmart.model.*;
import com.jdmart.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository,
                          RoleRepository roleRepository,
                          CategoryRepository categoryRepository,
                          ProductRepository productRepository,
                          CartRepository cartRepository,
                          AddressRepository addressRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Initializing JD Mart demo database seed...");

        // 1. Seed Roles
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_USER)));
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_ADMIN)));

        // 2. Seed Admin User
        if (!userRepository.existsByEmail("admin@jdmart.com")) {
            User admin = new User("JD Mart Admin", "admin@jdmart.com", "9876543210", passwordEncoder.encode("Admin@123"));
            admin.getRoles().add(userRole);
            admin.getRoles().add(adminRole);
            User savedAdmin = userRepository.save(admin);
            cartRepository.save(new Cart(savedAdmin));
            logger.info("Admin account seeded: admin@jdmart.com / Admin@123");
        }

        // 3. Seed Demo Users
        if (!userRepository.existsByEmail("rahul.sharma@example.com")) {
            User rahul = new User("Rahul Sharma", "rahul.sharma@example.com", "9812345678", passwordEncoder.encode("User@123"));
            rahul.getRoles().add(userRole);
            User savedRahul = userRepository.save(rahul);
            cartRepository.save(new Cart(savedRahul));

            Address addr = new Address("Rahul Sharma", "9812345678", "Flat 402, Shanti Heights", "MG Road, Indiranagar", "Bengaluru", "Karnataka", "560038");
            addr.setUser(savedRahul);
            addr.setDefault(true);
            addressRepository.save(addr);
            logger.info("Demo user seeded: rahul.sharma@example.com / User@123");
        }

        if (!userRepository.existsByEmail("priya.patel@example.com")) {
            User priya = new User("Priya Patel", "priya.patel@example.com", "9898765432", passwordEncoder.encode("User@123"));
            priya.getRoles().add(userRole);
            User savedPriya = userRepository.save(priya);
            cartRepository.save(new Cart(savedPriya));

            Address addr = new Address("Priya Patel", "9898765432", "B-12, Green Avenue", "Satellite Road", "Ahmedabad", "Gujarat", "380015");
            addr.setUser(savedPriya);
            addr.setDefault(true);
            addressRepository.save(addr);
            logger.info("Demo user seeded: priya.patel@example.com / User@123");
        }

        // 4. Seed Categories if empty
        if (categoryRepository.count() == 0) {
            seedCategoriesAndProducts();
        } else if (productRepository.count() < 30) {
            seedProducts();
        }

        logger.info("JD Mart database seeding completed successfully.");
    }

    private void seedCategoriesAndProducts() {
        Category catElectronics = categoryRepository.save(new Category("Electronics & Gadgets", "electronics", "Smartphones, Audio, Laptops and Personal Tech", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=60"));
        Category catFashion = categoryRepository.save(new Category("Fashion & Apparel", "fashion", "Ethnic, Casual, Western Wear and Wardrobe Essentials", "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=500&auto=format&fit=crop&q=60"));
        Category catFootwear = categoryRepository.save(new Category("Footwear & Shoes", "footwear", "Casual Sneakers, Formal Shoes, Running and Sports Footwear", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500&auto=format&fit=crop&q=60"));
        Category catHomeKitchen = categoryRepository.save(new Category("Home & Kitchen", "home-kitchen", "Kitchen Appliances, Cookware, Decor and Household Essentials", "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=500&auto=format&fit=crop&q=60"));
        Category catBeauty = categoryRepository.save(new Category("Beauty & Grooming", "beauty-grooming", "Skincare, Haircare, Fragrances and Personal Grooming", "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=500&auto=format&fit=crop&q=60"));
        Category catBooks = categoryRepository.save(new Category("Books & Stationery", "books-stationery", "Bestsellers, Academic, Self-Improvement and Office Supplies", "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500&auto=format&fit=crop&q=60"));
        Category catWatches = categoryRepository.save(new Category("Watches & Accessories", "watches-accessories", "Smartwatches, Analog Timepieces, Belts and Wallets", "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&auto=format&fit=crop&q=60"));
        Category catSports = categoryRepository.save(new Category("Sports & Fitness", "sports-fitness", "Gym Gear, Yoga Mats, Dumbbells and Sports Equipment", "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=500&auto=format&fit=crop&q=60"));

        createAllProducts(catElectronics, catFashion, catFootwear, catHomeKitchen, catBeauty, catBooks, catWatches, catSports);
    }

    private void seedProducts() {
        Category catElectronics = categoryRepository.findBySlug("electronics").orElse(null);
        Category catFashion = categoryRepository.findBySlug("fashion").orElse(null);
        Category catFootwear = categoryRepository.findBySlug("footwear").orElse(null);
        Category catHomeKitchen = categoryRepository.findBySlug("home-kitchen").orElse(null);
        Category catBeauty = categoryRepository.findBySlug("beauty-grooming").orElse(null);
        Category catBooks = categoryRepository.findBySlug("books-stationery").orElse(null);
        Category catWatches = categoryRepository.findBySlug("watches-accessories").orElse(null);
        Category catSports = categoryRepository.findBySlug("sports-fitness").orElse(null);

        if (catElectronics != null && catFashion != null && catFootwear != null) {
            createAllProducts(catElectronics, catFashion, catFootwear, catHomeKitchen, catBeauty, catBooks, catWatches, catSports);
        }
    }

    private void createAllProducts(Category catElectronics, Category catFashion, Category catFootwear,
                                   Category catHomeKitchen, Category catBeauty, Category catBooks,
                                   Category catWatches, Category catSports) {
        List<Product> products = new ArrayList<>();

        // Electronics (7 items)
        products.add(createProd("Samsung Galaxy M35 5G (Moonlight Blue, 128GB)",
                "6.6-inch Super AMOLED 120Hz display with massive 6000mAh battery and Exynos 1380 processor.",
                "The Samsung Galaxy M35 5G delivers unstoppable entertainment with a 6000mAh battery, vivid 120Hz FHD+ Super AMOLED display, and a 50MP No Shake OIS camera. Packed with Knox Security and 4 generations of OS upgrades.",
                21999.0, 27, 45, 4.4, 2140, "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=600&auto=format&fit=crop&q=80",
                "Display: 6.6\" FHD+ 120Hz AMOLED | RAM: 6GB | Storage: 128GB | Battery: 6000mAh | Camera: 50MP OIS + 8MP + 2MP", true, true, catElectronics));

        products.add(createProd("OnePlus Nord CE4 Lite 5G (Super Silver, 128GB)",
                "Sony LYT-600 50MP OIS camera, 80W SUPERVOOC charging, and 5500mAh battery.",
                "Experience everyday power with the OnePlus Nord CE4 Lite 5G. Featuring an ultra-bright 2,100 nits AMOLED display, reverse wired charging, and dual stereo speakers with 300% Ultra Volume Mode.",
                20999.0, 15, 30, 4.3, 1680, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600&auto=format&fit=crop&q=80",
                "Processor: Snapdragon 695 5G | Charging: 80W SUPERVOOC | Display: 120Hz AMOLED | Battery: 5500mAh", true, false, catElectronics));

        products.add(createProd("boAt Nirvana Ion ANC Wireless Earbuds",
                "Active Noise Cancellation up to 32dB with crystal bionic sound powered by HiFi DSP.",
                "Take listening to the next level with boAt Nirvana Ion ANC earbuds offering up to 120 hours total playtime, dual EQ modes, in-ear detection, and quad mics with ENx technology for crystal-clear calls.",
                9990.0, 75, 80, 4.2, 4520, "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=600&auto=format&fit=crop&q=80",
                "Playtime: 120 Hours | ANC: Up to 32dB | Bluetooth: v5.2 | Latency: 60ms BEAST Mode | IPX4 Water Resistant", true, true, catElectronics));

        products.add(createProd("Sony WH-CH520 Wireless Bluetooth Headphones",
                "On-Ear headphones with up to 50 hours battery life and DSEE sound enhancement.",
                "Enjoy high-quality sound throughout the day with Sony WH-CH520 wireless headphones. Features multi-point connection, 3-minute quick charge for 1.5 hours playback, and customizable EQ via Headphones Connect app.",
                4990.0, 20, 35, 4.5, 3100, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80",
                "Battery: Up to 50 Hours | Multi-point Bluetooth | Built-in Mic with Noise Suppression | Fast Pair Support", false, true, catElectronics));

        products.add(createProd("HP 15s Intel Core i3 12th Gen Thin & Light Laptop",
                "15.6-inch FHD micro-edge anti-glare display, 8GB DDR4 RAM, 512GB NVMe SSD.",
                "Built for performance and productivity, the HP 15s laptop is lightweight at just 1.69kg. Backed by fast charging, dual array digital microphones, and pre-installed Windows 11 Home & MS Office 2021.",
                46990.0, 23, 20, 4.2, 940, "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=600&auto=format&fit=crop&q=80",
                "Processor: Intel Core i3-1215U | RAM: 8GB DDR4 (Expandable) | Storage: 512GB SSD | Weight: 1.69 kg | OS: Win 11", false, false, catElectronics));

        products.add(createProd("Xiaomi Pad 6 (11-Inch, Snapdragon 870, 128GB)",
                "144Hz 2.8K display with Dolby Vision Atmos and quad stereo speakers.",
                "Unleash flagship entertainment and work productivity with Xiaomi Pad 6. Aluminum unibody design, Snapdragon 870 processor, 8840mAh battery with 33W fast charger included.",
                39999.0, 38, 25, 4.6, 2890, "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=600&auto=format&fit=crop&q=80",
                "Display: 11\" 2.8K 144Hz | SoC: Snapdragon 870 | Battery: 8840mAh | Audio: Quad Dolby Atmos Speakers", false, true, catElectronics));

        products.add(createProd("Mi 10000mAh Power Bank 3i with 18W Fast Charging",
                "Dual input (Type-C & Micro USB) and dual USB output with 12-layer circuit protection.",
                "Reliable, sleek metallic power bank built with high-density lithium polymer batteries. Supports two-way 18W fast charging and smart low-current charging mode for fitness bands.",
                2199.0, 40, 150, 4.4, 8740, "https://images.unsplash.com/photo-1609592426868-b78f4a187747?w=600&auto=format&fit=crop&q=80",
                "Capacity: 10000mAh | Output: Dual USB-A 18W | Input: Type-C & Micro USB | Body: Aluminum Alloy", false, false, catElectronics));

        // Fashion & Apparel (6 items)
        products.add(createProd("Raymond Premium Pure Cotton Solid Casual Shirt",
                "Breathable 100% combed cotton spread collar regular fit shirt in pristine navy blue.",
                "Crafted from the finest long-staple cotton, this Raymond shirt combines elegance with all-day comfort. Machine washable with pre-shrunk fabric and reinforced mother-of-pearl buttons.",
                2499.0, 48, 60, 4.3, 1280, "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=600&auto=format&fit=crop&q=80",
                "Material: 100% Premium Cotton | Fit: Regular | Collar: Spread | Sleeve: Full Sleeve | Pattern: Solid Navy", true, true, catFashion));

        products.add(createProd("Levi's Men's 511 Slim Fit Stretchable Denim Jeans",
                "Classic five-pocket styling with flexible stretch denim in dark indigo stone wash.",
                "The authentic modern slim fit from Levi's. Cut slim through the thigh with a straight leg opening, woven with elastane for effortless comfort during active movement.",
                3999.0, 45, 40, 4.4, 3420, "https://images.unsplash.com/photo-1542272604-780c96856592?w=600&auto=format&fit=crop&q=80",
                "Fabric: 98% Cotton, 2% Elastane | Fit: 511 Slim | Rise: Mid Rise | Wash: Dark Indigo Stone", true, false, catFashion));

        products.add(createProd("Biba Women Embroidered Anarkali Kurta with Dupatta",
                "Rich Chanderi silk blend Anarkali suit set with intricate Zari and thread handwork.",
                "Celebrate festivities in grand style with this Biba Anarkali kurta set. Includes a flared calf-length kurta, solid matching churidar, and an embellished organza dupatta.",
                4599.0, 52, 35, 4.5, 870, "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=600&auto=format&fit=crop&q=80",
                "Fabric: Chanderi Silk Blend | Length: Calf Length | Neck: Round Neck | Occasion: Festive & Wedding", true, true, catFashion));

        products.add(createProd("U.S. Polo Assn. Men's Solid Cotton Pique Polo T-Shirt",
                "Signature embroidered brand logo on chest with ribbed collar and taped side slits.",
                "A wardrobe staple made from breathable cotton pique knit. Designed to resist color fading and maintain collar crispness even after repeated washing.",
                1799.0, 45, 90, 4.1, 2150, "https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=600&auto=format&fit=crop&q=80",
                "Material: 100% Pique Cotton | Fit: Regular | Collar: Ribbed Polo | Sleeve: Half Sleeve", false, false, catFashion));

        products.add(createProd("Aurelia Women's Floral Print Straight Cotton Kurti",
                "Round neck daily wear straight cotton kurti with three-quarter bell sleeves.",
                "Elevate your daily office and casual wear with this soft, breathable printed kurti from Aurelia. Paired effortlessly with palazzos, leggings, or jeans.",
                1299.0, 50, 75, 4.3, 1140, "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=600&auto=format&fit=crop&q=80",
                "Fabric: 100% Breathable Cotton | Style: Straight Cut | Print: Artisan Floral | Machine Washable", false, false, catFashion));

        products.add(createProd("Allen Solly Men's Casual Khaki Chino Trousers",
                "Flat-front comfort fit stretch chinos with coin pocket and concealed side pockets.",
                "Versatile chinos tailored for smart-casual Fridays and weekend getaways. Made with cotton twill engineered with soft-wash finish.",
                2299.0, 40, 50, 4.2, 730, "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=600&auto=format&fit=crop&q=80",
                "Material: 97% Cotton, 3% Spandex | Fit: Slim-Regular | Closure: Button & Zip Fly | Pocket: 4 Pockets", false, false, catFashion));

        // Footwear & Shoes (4 items)
        products.add(createProd("Red Tape Men's Classic Low-Top White Sneakers",
                "Memory foam insole with durable TPR sole and premium PU leather upper.",
                "Stride in unmatched urban style. Features a cushioned collar, shock-absorbing midsole, and anti-skid grip pattern for all-day streetwear comfort.",
                4899.0, 76, 120, 4.4, 5230, "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop&q=80",
                "Sole: TPR Anti-skid | Insole: Memory Foam | Upper: High Grade Synthetic Leather | Closure: Lace-Up", true, true, catFootwear));

        products.add(createProd("Sparx Men's Ultra Light Mesh Running Shoes",
                "Breathable knitted mesh upper with responsive EVA midsole cushioning.",
                "Designed for daily workouts, morning jogging, and sports activities. Extremely light at 220g per shoe, keeping feet cool and fatigue-free.",
                1499.0, 35, 85, 4.2, 3890, "https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=600&auto=format&fit=crop&q=80",
                "Sole: EVA Phylon | Upper: Knitted Mesh | Weight: 220g | Closure: Lace-up | Usage: Running / Walking", false, true, catFootwear));

        products.add(createProd("Bata Men's Formal Derby Shoes (Genuine Leather)",
                "Hand-finished genuine leather formal lace-ups with padded footbed.",
                "An essential pair for business meetings, weddings, and formal evenings. Handcrafted with rich genuine leather and durable non-slip PU outsole.",
                2999.0, 30, 45, 4.3, 1420, "https://images.unsplash.com/photo-1614252235316-8c857d38b5f4?w=600&auto=format&fit=crop&q=80",
                "Material: 100% Genuine Leather | Sole: PU | Type: Derby | Color: Glossy Tan Brown", false, false, catFootwear));

        products.add(createProd("Campus Women's Oxyfit Running & Walking Shoes",
                "Yoga Flex sole with pillowy Springy Fit footbed and engineered mesh body.",
                "Experience feather-soft walking support with Campus Oxyfit for women. Flexible sole flexes naturally with the foot arch during every step.",
                1899.0, 42, 65, 4.3, 2190, "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=600&auto=format&fit=crop&q=80",
                "Sole: Phylon Yoga-Flex | Insole: Memory Spring | Upper: Air Mesh | Washable: Yes", false, false, catFootwear));

        // Home & Kitchen (4 items)
        products.add(createProd("Prestige Deluxe Alpha Stainless Steel Pressure Cooker (3L)",
                "Heavy alpha base with unique pressure indicator and durable stainless steel body.",
                "Cook healthy Indian curries, lentils, and biryanis faster with the Prestige Deluxe Alpha cooker. Induction and gas stove compatible with dual safety valves.",
                3250.0, 26, 50, 4.5, 4120, "https://images.unsplash.com/photo-1584269600464-37b1b58a9fe7?w=600&auto=format&fit=crop&q=80",
                "Capacity: 3 Liters | Material: 18/10 Stainless Steel | Base: Alpha Induction Bottom | Warranty: 5 Years", true, false, catHomeKitchen));

        products.add(createProd("Philips Digital Air Fryer with Rapid Air Tech (4.1L)",
                "Fry with up to 90% less fat using patented Rapid Air vortex technology.",
                "Enjoy crispy samosas, french fries, grilled paneer, and chicken tikka guilt-free. Touchscreen with 7 preset cooking modes and keep-warm function.",
                10995.0, 32, 25, 4.6, 2930, "https://images.unsplash.com/photo-1585515320310-259814833e62?w=600&auto=format&fit=crop&q=80",
                "Capacity: 4.1L (0.8kg food) | Power: 1400W | Control: Digital Touch | Auto Shut-off: Yes", true, true, catHomeKitchen));

        products.add(createProd("Pigeon by Stovekraft 750W 3-Jar Mixer Grinder",
                "Heavy duty copper motor with stainless steel jars for wet, dry, and chutney grinding.",
                "Grind hard turmeric, idli batter, and chutneys in minutes. Features overload safety protection, stainless steel blades, and sturdy anti-slip suction feet.",
                3495.0, 52, 70, 4.1, 5600, "https://images.unsplash.com/photo-1574269909862-7e1d70bb8078?w=600&auto=format&fit=crop&q=80",
                "Motor: 750W Copper | Jars: 1.5L Wet, 1.0L Dry, 0.4L Chutney | Speed: 3 Speeds with Pulse", false, false, catHomeKitchen));

        products.add(createProd("Milton Thermosteel Flip Lid Insulated Water Bottle (1000ml)",
                "Double wall vacuum insulated flask keeping beverages hot or cold for 24 hours.",
                "Constructed with food-grade 304 stainless steel. 100% leak proof with durable carry strap, ideal for gym, office, travel, and school use.",
                1195.0, 20, 110, 4.6, 8400, "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=600&auto=format&fit=crop&q=80",
                "Capacity: 1000ml | Insulation: 24h Hot & Cold | Grade: SS 304 Rust-free | BPA Free", false, true, catHomeKitchen));

        // Beauty & Grooming (3 items)
        products.add(createProd("Philips BT1232 Cordless Beard Trimmer with USB Charging",
                "Skin-friendly self-sharpening stainless steel blades with 30-minute cordless run time.",
                "Achieve zero trim or 1mm, 5mm, and 7mm stubble with durable comb attachments. USB charging cable included for convenient travel grooming.",
                1195.0, 25, 80, 4.3, 7620, "https://images.unsplash.com/photo-1621607512214-68297480165e?w=600&auto=format&fit=crop&q=80",
                "Blades: Self-sharpening Steel | Run time: 30 Mins | Charging: USB | Warranty: 2 Years", true, false, catBeauty));

        products.add(createProd("Mamaearth Onion Hair Oil with Redensyl for Hair Fall Control",
                "Contains blend of onion seed oil, castor oil, almond oil, and bhringraj.",
                "Dermatologically tested toxin-free hair oil that boosts blood circulation in scalp, strengthens roots, reduces hair thinning, and promotes shine.",
                599.0, 20, 130, 4.2, 4980, "https://images.unsplash.com/photo-1608248597359-009139046c8a?w=600&auto=format&fit=crop&q=80",
                "Volume: 250ml | Ingredients: Onion Oil, Redensyl, Bhringraj | Paraben & Sulfate Free", false, false, catBeauty));

        products.add(createProd("The Derma Co 1% Hyaluronic Sunscreen Aqua Gel (SPF 50 PA++++ )",
                "Ultra-lightweight fragrance-free broad spectrum protection without white cast.",
                "Formulated with hyaluronic acid and vitamin E. Absorbs instantly like water, protecting against UVA/UVB rays and digital blue light.",
                499.0, 15, 140, 4.5, 6300, "https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&auto=format&fit=crop&q=80",
                "SPF: 50 PA++++ | Weight: 50g | Finish: Non-greasy, Dewy | Safe for all skin types", false, true, catBeauty));

        // Books & Stationery (3 items)
        products.add(createProd("Atomic Habits by James Clear (Paperback)",
                "An easy & proven way to build good habits & break bad ones - #1 International Bestseller.",
                "Over 15 million copies sold globally. A transformative practical guide teaching small atomic changes that yield monumental lifelong results.",
                799.0, 45, 95, 4.8, 14200, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&auto=format&fit=crop&q=80",
                "Author: James Clear | Publisher: Random House | Pages: 320 | Language: English | Format: Paperback", true, true, catBooks));

        products.add(createProd("The Psychology of Money by Morgan Housel",
                "Timeless lessons on wealth, greed, and happiness by award-winning author Morgan Housel.",
                "Doing well with money isn't necessarily about what you know. It's about how you behave. 19 short stories exploring the strange ways people think about money.",
                450.0, 35, 110, 4.7, 9840, "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600&auto=format&fit=crop&q=80",
                "Author: Morgan Housel | Publisher: Jaico Publishing | Pages: 252 | Language: English", false, true, catBooks));

        products.add(createProd("Classmate Pulse Premium Spiral Notebook (Pack of 3)",
                "Single line ruled, 300 pages each, elemental chlorine-free paper with micro-perforation.",
                "Durable poly cover protects pages from spills and bends. Ideal for college lectures, office notes, and daily journaling.",
                480.0, 20, 160, 4.5, 2310, "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=600&auto=format&fit=crop&q=80",
                "Pack: 3 Notebooks | Binding: Wirebound Spiral | Pages: 300 per book | Size: A4", false, false, catBooks));

        // Watches & Accessories (4 items)
        products.add(createProd("Noise ColorFit Pulse 3 Smartwatch (1.96-Inch AMOLED)",
                "Bluetooth calling, 150+ watch faces, advanced heart rate & SpO2 tracking with IP68 rating.",
                "Stay seamlessly connected with crystal clear wrist calls, 7-day battery life, 100 sports tracking modes, and premium metallic dial finish.",
                4999.0, 72, 85, 4.2, 5920, "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80",
                "Display: 1.96\" AMOLED | Battery: 7 Days | Water Resistance: IP68 | Connectivity: Bluetooth 5.3", true, true, catWatches));

        products.add(createProd("Titan Neo Analog Men's Watch (Anthracite Dial)",
                "Mineral glass, water resistant to 50 meters with genuine brown leather strap.",
                "Classic understated elegance from Titan's Neo collection. Designed with a sleek stainless steel bezel, date calendar aperture, and quartz precision.",
                4495.0, 25, 40, 4.6, 2140, "https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=600&auto=format&fit=crop&q=80",
                "Brand: Titan | Strap: Genuine Leather | Water Resistance: 50m | Movement: Quartz | Warranty: 2 Years", true, false, catWatches));

        products.add(createProd("WildHorn Genuine Leather Men's RFID Blocking Wallet",
                "Handcrafted top-grain hunter leather wallet with 9 card slots and 2 currency compartments.",
                "Equipped with advanced RFID SECURE technology to block scanning devices and protect sensitive debit/credit card information.",
                1499.0, 60, 90, 4.4, 4310, "https://images.unsplash.com/photo-1627123424574-724758594e93?w=600&auto=format&fit=crop&q=80",
                "Material: Genuine Hunter Leather | Security: RFID Blocking | Slots: 9 Card + 2 Currency", false, false, catWatches));

        products.add(createProd("Fastrack Men's Aviator UV Protected Sunglasses",
                "Lightweight gunmetal alloy frame with green 100% UV400 protective lenses.",
                "Timeless teardrop aviator frame suitable for all face shapes. Protects eyes from glare during driving, outdoor sports, and beach travel.",
                1899.0, 35, 65, 4.3, 1780, "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=600&auto=format&fit=crop&q=80",
                "Frame: Metal Alloy | Lens: UV400 Protection | Style: Pilot Aviator | Includes: Hard Case", false, true, catWatches));

        // Sports & Fitness (3 items)
        products.add(createProd("Boldfit Anti-Skid Yoga Mat for Men & Women (6mm)",
                "High-density eco-friendly TPE material with carrying strap and alignment lines.",
                "Provides optimum cushioning for knees, joints, and spine during yoga, pilates, stretching, and floor calisthenics exercises.",
                1499.0, 47, 80, 4.5, 3840, "https://images.unsplash.com/photo-1545205597-3d9d02c29597?w=600&auto=format&fit=crop&q=80",
                "Thickness: 6mm | Material: Eco TPE | Feature: Dual Anti-Slip Texture | Includes: Free Strap", true, false, catSports));

        products.add(createProd("Kore PVC 20kg Home Gym Dumbbells & Barbell Kit",
                "Includes 20kg weight plates, dumbbell rods, 3ft curl rod, skipping rope, and gym gloves.",
                "Complete home fitness starter set allowing bench presses, bicep curls, shoulder raises, and full body conditioning workouts.",
                2999.0, 55, 35, 4.0, 2460, "https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?w=600&auto=format&fit=crop&q=80",
                "Weight: 20kg Total | Plates: 2kg x 4, 3kg x 4 | Rods: 3ft Curl Rod + Dumbbell Rods", false, false, catSports));

        products.add(createProd("Yonex Nanoray 7000I Graphite Badminton Racquet",
                "Ultra-light 77g aluminum-graphite composite frame with isometric head shape.",
                "Provides rapid swing recovery and high repulsive power for attacking smashes and controlled net shots. Pre-strung with high tension Yonex string.",
                2290.0, 30, 50, 4.4, 3120, "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=600&auto=format&fit=crop&q=80",
                "Weight: 4U (77g) | Frame: Graphite/Aluminum | Tension: 24-28 lbs | Includes: Full Cover", false, true, catSports));

        productRepository.saveAll(products);
        logger.info("Seeded {} demo products across 8 categories.", products.size());
    }

    private Product createProd(String name, String shortDesc, String desc, Double originalPrice,
                               int discountPct, int stock, double rating, int reviewCount,
                               String imageUrl, String specs, boolean featured, boolean trending, Category category) {
        Product p = new Product();
        p.setName(name);
        p.setShortDescription(shortDesc);
        p.setDescription(desc);
        p.setOriginalPrice(originalPrice);
        p.setDiscountPercentage(discountPct);
        double sellingPrice = Math.round(originalPrice * (1.0 - (discountPct / 100.0)) * 100.0) / 100.0;
        p.setSellingPrice(sellingPrice);
        p.setStockQuantity(stock);
        p.setRating(rating);
        p.setReviewCount(reviewCount);
        p.setImageUrl(imageUrl);
        p.setSpecifications(specs);
        p.setFeatured(featured);
        p.setTrending(trending);
        p.setActive(true);
        p.setCategory(category);
        return p;
    }
}
