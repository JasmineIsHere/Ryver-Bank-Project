package ryver.app.customer;

import ryver.app.asset.*;
import ryver.app.portfolio.*;

import java.util.*;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@SecurityRequirement(name = "api")
public class CustomerController {
  // Repositories and the encoder
  private CustomerRepository customers;
  private PortfolioRepository portfolios;
  private BCryptPasswordEncoder encoder;

  public CustomerController(CustomerRepository customers, PortfolioRepository portfolios,
      BCryptPasswordEncoder encoder) {
    this.customers = customers;
    this.portfolios = portfolios;
    this.encoder = encoder;
  }

  /**
   * Get all Customers with ROLE_USER
   * Returns 200 OK (if no exceptions)
   * 
   * @return List<Customer>
   */
  @GetMapping("/api/customers")
  public List<Customer> getCustomers() {
    return customers.findByAuthorities("ROLE_USER");
  }

  /**
   * Get a specific Customer, based on the specified customerId
   * If Customer not found - Returns 400 Bad Request
   * Returns 200 OK (if no exceptions)
   * 
   * @param customerId
   * @return Customer
   */
  @GetMapping("/api/customers/{customerId}")
  public Customer getSpecificCustomer(@PathVariable(value = "customerId") Long customerId) {
    String authorisedUser = SecurityContextHolder.getContext().getAuthentication().getName();
    Object[] authorityArray = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .toArray();
    String authority = authorityArray[0].toString();
    Customer customer = customers.findById(customerId).orElseThrow(() -> new CustomerNotFoundException(customerId));

    if (!customer.isActive()) {
      throw new InactiveCustomerException();
    }

    if (!customer.getUsername().equals(authorisedUser) && authority.equals("ROLE_USER")) {
      throw new CustomerMismatchException();
    }
    return customer;
  }

  /**
   * Update a Customer, based on JSON data
   * Managers can update (active or not active) customers information (phone,
   * address, password, active)
   * Active customer can update OWN information (phone, address, password)
   * Deactivated customer cannot update OWN information
   * Returns 200 OK (if no exceptions)
   * 
   * @param customerId
   * @param updatedCustomerInfo
   * @return Customer
   */
  @PutMapping("/api/customers/{customerId}")
  public Customer updateCustomer(@PathVariable(value = "customerId") Long customerId,
      @Valid @RequestBody Customer updatedCustomerInfo) {

    String authorisedUser = SecurityContextHolder.getContext().getAuthentication().getName();
    Object[] authorityArray = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .toArray();
    String authority = authorityArray[0].toString();
    Customer customer = customers.findById(customerId).orElseThrow(() -> new CustomerNotFoundException(customerId));

    if (!customer.getUsername().equals(authorisedUser) && authority.equals("ROLE_USER")) {
      throw new CustomerMismatchException();
    }
    // Fields which customers and managers can update - password, phone, address
    customer.setPassword(encoder.encode(updatedCustomerInfo.getPassword()));
    customer.setPhone(updatedCustomerInfo.getPhone());
    customer.setAddress(updatedCustomerInfo.getAddress());

    // Fields which only managers can update - active
    if (authority.equals("ROLE_MANAGER")) { //if customer is active/inactive, manager can change
      customer.setActive(updatedCustomerInfo.isActive());
    } else if (!customer.isActive()) { //if customer already not active, and non-manager trying to change details, throw exception
      throw new InactiveCustomerException();
    }
    customers.save(customer);
    return customer;
  }

  /**
   * Create a new Customer, based on JSON data
   * A Portfolio for the new Customer is also created
   * Using BCrypt encoder to encrypt the password for storage
   * If ROLE_USER - Returns 403 Forbidden
   * Returns 201 Created (if no exceptions)
   * 
   * @param customer
   * @return Customer
   */
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/api/customers")
  public Customer addCustomer(@Valid @RequestBody Customer customer) {
    // Check the list of existing users for a duplicate username & nric
    // Each person can only have one user account
    List<Customer> customerList = customers.findAll();

    for (Customer existingCustomer : customerList) {
      if (customer.getUsername().equals(existingCustomer.getUsername())
          || customer.getNric().equals(existingCustomer.getNric())) {
        throw new UserAlreadyExistsException(customer.getUsername());
      }
    }

    customer.setPassword(encoder.encode(customer.getPassword()));
    if (!validateNric(customer.getNric()))
      throw new InvalidNricException();

    Portfolio portfolio = addPortfolio(customers.save(customer));
    customer.setPortfolio(portfolio);
    return customers.save(customer);

  }

  /**
   * Create a Portfolio for a specified Customer Called in addCustomer
   * 
   * @param customer
   * @return Portfolio
   */
  public Portfolio addPortfolio(Customer customer) {
    // Only customers have portfolios
    ArrayList<SimpleGrantedAuthority> a = new ArrayList(customer.getAuthorities());
    if (a.get(0).getAuthority().equals("ROLE_USER")) {
      Portfolio portfolio = new Portfolio();

      portfolio.setCustomer(customer);

      portfolio.setCustomer_id(customer.getId());

      portfolio.setAssets(new ArrayList<Asset>());

      return portfolios.save(portfolio);
    } else
      return null;
  }

  /**
   * Checks if the specified NRIC is valid
   * 
   * @param nric
   * @return boolean
   */
  public static boolean validateNric(String nric) {
    int total = 0;
    int arr[] = { 2, 7, 6, 5, 4, 3, 2 };
    char charArr[] = nric.toCharArray();

    if (nric.length() - 2 != arr.length)
      return false;

    for (int i = 0; i < arr.length; i++) {
      total += arr[i] * Character.getNumericValue(charArr[i + 1]);
    }

    if (charArr[0] == 'T' || charArr[0] == 'G')
      total += 4;

    if (charArr[0] == 'S' || charArr[0] == 'T') {
      switch (total % 11) {
        case 0:
          if (charArr[charArr.length - 1] == 'J')
            return true;
          break;
        case 1:
          if (charArr[charArr.length - 1] == 'Z')
            return true;
          break;
        case 2:
          if (charArr[charArr.length - 1] == 'I')
            return true;
          break;
        case 3:
          if (charArr[charArr.length - 1] == 'H')
            return true;
          break;
        case 4:
          if (charArr[charArr.length - 1] == 'G')
            return true;
          break;
        case 5:
          if (charArr[charArr.length - 1] == 'F')
            return true;
          break;
        case 6:
          if (charArr[charArr.length - 1] == 'E')
            return true;
          break;
        case 7:
          if (charArr[charArr.length - 1] == 'D')
            return true;
          break;
        case 8:
          if (charArr[charArr.length - 1] == 'C')
            return true;
          break;
        case 9:
          if (charArr[charArr.length - 1] == 'B')
            return true;
          break;
        case 10:
          if (charArr[charArr.length - 1] == 'A')
            return true;
          break;
        default:
          return false;
      }
    } else if (charArr[0] == 'F' || charArr[0] == 'G') {
      switch (total % 11) {
        case 0:
          if (charArr[charArr.length - 1] == 'X')
            return true;
          break;
        case 1:
          if (charArr[charArr.length - 1] == 'W')
            return true;
          break;
        case 2:
          if (charArr[charArr.length - 1] == 'U')
            return true;
          break;
        case 3:
          if (charArr[charArr.length - 1] == 'T')
            return true;
          break;
        case 4:
          if (charArr[charArr.length - 1] == 'R')
            return true;
          break;
        case 5:
          if (charArr[charArr.length - 1] == 'Q')
            return true;
          break;
        case 6:
          if (charArr[charArr.length - 1] == 'P')
            return true;
          break;
        case 7:
          if (charArr[charArr.length - 1] == 'N')
            return true;
          break;
        case 8:
          if (charArr[charArr.length - 1] == 'M')
            return true;
          break;
        case 9:
          if (charArr[charArr.length - 1] == 'L')
            return true;
          break;
        case 10:
          if (charArr[charArr.length - 1] == 'K')
            return true;
          break;
        default:
          return false;
      }
    }
    return false;
  }

}