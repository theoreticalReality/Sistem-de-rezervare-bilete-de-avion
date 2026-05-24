package com.ProiectIS.GestionareAeroport.controller;

import com.ProiectIS.GestionareAeroport.dto.AirlineLoginRequest;
import com.ProiectIS.GestionareAeroport.dto.BookingRequest;
import com.ProiectIS.GestionareAeroport.dto.BookingResponse;
import com.ProiectIS.GestionareAeroport.dto.CreateRegularFlightRequest;
import com.ProiectIS.GestionareAeroport.dto.CreateSeasonalFlightRequest;
import com.ProiectIS.GestionareAeroport.dto.PassengerDto;
import com.ProiectIS.GestionareAeroport.dto.SearchQuery;
import com.ProiectIS.GestionareAeroport.dto.SearchResult;
import com.ProiectIS.GestionareAeroport.dto.StaffLoginRequest;
import com.ProiectIS.GestionareAeroport.model.AirlineCompany;
import com.ProiectIS.GestionareAeroport.model.AirportStaff;
import com.ProiectIS.GestionareAeroport.model.Booking;
import com.ProiectIS.GestionareAeroport.model.Flight;
import com.ProiectIS.GestionareAeroport.model.RegularFlight;
import com.ProiectIS.GestionareAeroport.model.SeasonalFlight;
import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import com.ProiectIS.GestionareAeroport.model.enums.PaymentMethod;
import com.ProiectIS.GestionareAeroport.repository.AirportStaffRepository;
import com.ProiectIS.GestionareAeroport.service.AirlineCompanyService;
import com.ProiectIS.GestionareAeroport.service.BookingService;
import com.ProiectIS.GestionareAeroport.service.FlightSearchService;
import com.ProiectIS.GestionareAeroport.service.FlightService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PortalController {

    private final AirlineCompanyService airlineService;
    private final FlightService flightService;
    private final FlightSearchService flightSearchService;
    private final BookingService bookingService;
    private final AirportStaffRepository staffRepository;

    public PortalController(AirlineCompanyService airlineService,
                            FlightService flightService,
                            FlightSearchService flightSearchService,
                            BookingService bookingService,
                            AirportStaffRepository staffRepository) {
        this.airlineService = airlineService;
        this.flightService = flightService;
        this.flightSearchService = flightSearchService;
        this.bookingService = bookingService;
        this.staffRepository = staffRepository;
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        model.addAttribute("companyLoggedIn", session.getAttribute("loggedInCompany") != null);
        model.addAttribute("staffLoggedIn", session.getAttribute("loggedInStaff") != null);
        return "home";
    }

    @GetMapping("/flights/search")
    public String passengerSearch(@ModelAttribute("query") SearchQuery query,
                                  @RequestParam(defaultValue = "false") boolean searched,
                                  Model model) {
        if (query.getNumberOfPassengers() == null) {
            query.setNumberOfPassengers(1);
        }
        if (query.getWantsReturn() == null) {
            query.setWantsReturn(false);
        }

        model.addAttribute("searched", searched);
        if (searched) {
            try {
                SearchResult result = flightSearchService.search(query);
                model.addAttribute("result", result);
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }

        return "flight-search";
    }

    @GetMapping("/bookings/new")
    public String newBooking(@RequestParam Long outboundFlightId,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate outboundDate,
                             @RequestParam(required = false) Long returnFlightId,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
                             @RequestParam(required = false) Integer passengerCount,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            BookingRequest request = new BookingRequest();
            request.setOutboundFlightId(outboundFlightId);
            request.setOutboundDate(outboundDate);
            request.setReturnFlightId(returnFlightId);
            request.setReturnDate(returnDate);
            request.setSelectedClass(ClassType.ECONOMY);
            request.setPaymentMethod(PaymentMethod.CARD);

            PassengerDto passenger = new PassengerDto();
            passenger.setNumberOfAdults(passengerCount == null || passengerCount <= 0 ? 1 : passengerCount);
            passenger.setNumberOfChildren(0);
            passenger.setNumberOfSeniors(0);
            request.setPassenger(passenger);

            model.addAttribute("bookingRequest", request);
            addBookingFormAttributes(model, request);
            return "booking-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/flights/search";
        }
    }

    @PostMapping("/bookings")
    public String createPublicBooking(@ModelAttribute("bookingRequest") BookingRequest request,
                                      Model model) {
        try {
            Booking booking = bookingService.createBooking(request);
            model.addAttribute("booking", BookingResponse.fromEntity(booking));
            return "booking-confirmation";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            addBookingFormAttributes(model, request);
            return "booking-form";
        }
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new AirlineLoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute AirlineLoginRequest loginRequest, HttpSession session, RedirectAttributes redirectAttributes) {
        System.out.println("Tentativă de login pentru: " + loginRequest.getEmail());
        try {
            AirlineCompany company = airlineService.login(loginRequest.getEmail(), loginRequest.getPassword());
            System.out.println("Login reușit pentru: " + company.getName());
            session.setAttribute("loggedInCompany", company);
            return "redirect:/dashboard";
        } catch (Exception e) {
            System.out.println("Login eșuat: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Email/ID companie sau parolă incorecte.");
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/staff/login")
    public String staffLoginPage(Model model) {
        model.addAttribute("loginRequest", new StaffLoginRequest());
        return "staff-login";
    }

    @PostMapping("/staff/login")
    public String staffLogin(@ModelAttribute StaffLoginRequest loginRequest,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            AirportStaff staff = staffRepository.findByPersonalCode(loginRequest.getPersonalCode())
                    .orElseThrow(() -> new IllegalArgumentException("Cod personal invalid."));
            session.setAttribute("loggedInStaff", staff);
            return "redirect:/staff/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cod personal invalid.");
            return "redirect:/staff/login";
        }
    }

    @GetMapping("/staff/dashboard")
    public String staffDashboard(@RequestParam(required = false) String flightCode,
                                 HttpSession session,
                                 Model model) {
        AirportStaff staff = (AirportStaff) session.getAttribute("loggedInStaff");
        if (staff == null) {
            return "redirect:/staff/login";
        }

        String normalizedFlightCode = flightCode == null ? "" : flightCode.trim();
        model.addAttribute("staff", staff);
        model.addAttribute("selectedFlightCode", normalizedFlightCode);
        model.addAttribute("flights", flightService.findAllDtos());

        if (!normalizedFlightCode.isBlank()) {
            model.addAttribute("bookings", bookingService.findResponsesByFlightCode(normalizedFlightCode));
        }

        return "staff-dashboard";
    }

    @PostMapping("/staff/bookings/{bookingId}/confirm-payment")
    public String staffConfirmCashPayment(@PathVariable String bookingId,
                                          @RequestParam(required = false) String flightCode,
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInStaff") == null) {
            return "redirect:/staff/login";
        }

        try {
            bookingService.confirmCashPayment(bookingId);
            redirectAttributes.addFlashAttribute("success", "Plata cash a fost validată.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        String suffix = flightCode == null || flightCode.isBlank() ? "" : "?flightCode=" + flightCode.trim();
        return "redirect:/staff/dashboard" + suffix;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        AirlineCompany company = (AirlineCompany) session.getAttribute("loggedInCompany");
        if (company == null) {
            return "redirect:/login";
        }
        model.addAttribute("company", company);
        model.addAttribute("flights", flightService.findDtosByAirline(company.getId()));
        return "dashboard";
    }

    @GetMapping("/add-flight")
    public String addFlightPage(HttpSession session, Model model) {
        if (session.getAttribute("loggedInCompany") == null) {
            return "redirect:/login";
        }
        model.addAttribute("regularRequest", new CreateRegularFlightRequest());
        model.addAttribute("seasonalRequest", new CreateSeasonalFlightRequest());
        return "add-flight";
    }

    @PostMapping("/add-regular-flight")
    public String addRegularFlight(@ModelAttribute CreateRegularFlightRequest request,
                                   @RequestParam Map<String, String> allParams,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        AirlineCompany company = (AirlineCompany) session.getAttribute("loggedInCompany");
        if (company == null) return "redirect:/login";

        try {
            Map<ClassType, Integer> seats = new HashMap<>();
            Map<ClassType, Double> prices = new HashMap<>();

            seats.put(ClassType.BUSINESS, Integer.parseInt(allParams.get("seats_BUSINESS")));
            seats.put(ClassType.FIRST_CLASS, Integer.parseInt(allParams.get("seats_FIRST")));
            seats.put(ClassType.ECONOMY, Integer.parseInt(allParams.get("seats_ECONOMY")));

            prices.put(ClassType.BUSINESS, Double.parseDouble(allParams.get("prices_BUSINESS")));
            prices.put(ClassType.FIRST_CLASS, Double.parseDouble(allParams.get("prices_FIRST")));
            prices.put(ClassType.ECONOMY, Double.parseDouble(allParams.get("prices_ECONOMY")));

            request.setSeats(seats);
            request.setPrices(prices);

            flightService.addRegularFlight(company.getId(), request);
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/add-flight";
        }
    }

    @PostMapping("/add-seasonal-flight")
    public String addSeasonalFlight(@ModelAttribute CreateSeasonalFlightRequest request,
                                    @RequestParam Map<String, String> allParams,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        AirlineCompany company = (AirlineCompany) session.getAttribute("loggedInCompany");
        if (company == null) return "redirect:/login";

        try {
            Map<ClassType, Integer> seats = new HashMap<>();
            Map<ClassType, Double> prices = new HashMap<>();

            seats.put(ClassType.BUSINESS, Integer.parseInt(allParams.get("seats_BUSINESS")));
            seats.put(ClassType.FIRST_CLASS, Integer.parseInt(allParams.get("seats_FIRST")));
            seats.put(ClassType.ECONOMY, Integer.parseInt(allParams.get("seats_ECONOMY")));

            prices.put(ClassType.BUSINESS, Double.parseDouble(allParams.get("prices_BUSINESS")));
            prices.put(ClassType.FIRST_CLASS, Double.parseDouble(allParams.get("prices_FIRST")));
            prices.put(ClassType.ECONOMY, Double.parseDouble(allParams.get("prices_ECONOMY")));
            
            // Parsare MonthDay (format MM-dd)
            request.setSeasonStart(MonthDay.parse("--" + allParams.get("seasonStartStr")));
            request.setSeasonEnd(MonthDay.parse("--" + allParams.get("seasonEndStr")));

            request.setSeats(seats);
            request.setPrices(prices);

            flightService.addSeasonalFlight(company.getId(), request);
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/add-flight";
        }
    }

    @GetMapping("/edit-flight/{flightCode}")
    public String editFlightPage(@PathVariable String flightCode, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        AirlineCompany company = (AirlineCompany) session.getAttribute("loggedInCompany");
        if (company == null) return "redirect:/login";

        try {
            Flight flight = flightService.findByCode(flightCode);
            if (flight.getAirline() == null || !flight.getAirline().getId().equals(company.getId())) {
                redirectAttributes.addFlashAttribute("error", "Nu poți edita un zbor care nu îți aparține.");
                return "redirect:/dashboard";
            }

            model.addAttribute("flight", flight);
            model.addAttribute("currentFlightCode", flightCode);
            model.addAttribute("isRegular", flight instanceof RegularFlight);
            model.addAttribute("isSeasonal", flight instanceof SeasonalFlight);
            addSeatAndPriceAttributes(model, flight);

            if (flight instanceof RegularFlight regularFlight) {
                model.addAttribute("daysOfWeek", regularFlight.getDaysOfWeek());
                model.addAttribute("departureTime", regularFlight.getDepartureTime());
                model.addAttribute("arrivalTime", regularFlight.getArrivalTime());
            } else if (flight instanceof SeasonalFlight seasonalFlight) {
                model.addAttribute("daysOfWeek", seasonalFlight.getDaysOfWeek());
                model.addAttribute("departureTime", seasonalFlight.getDepartureTime());
                model.addAttribute("arrivalTime", seasonalFlight.getArrivalTime());
                model.addAttribute("seasonStartStr", formatMonthDay(seasonalFlight.getSeasonStart()));
                model.addAttribute("seasonEndStr", formatMonthDay(seasonalFlight.getSeasonEnd()));
            }

            return "edit-flight";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/edit-regular-flight/{currentFlightCode}")
    public String editRegularFlight(@PathVariable String currentFlightCode,
                                    @ModelAttribute CreateRegularFlightRequest request,
                                    @RequestParam Map<String, String> allParams,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        AirlineCompany company = (AirlineCompany) session.getAttribute("loggedInCompany");
        if (company == null) return "redirect:/login";

        try {
            applySeatsAndPrices(request, allParams);
            flightService.updateRegularFlight(company.getId(), currentFlightCode, request);
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/edit-flight/" + currentFlightCode;
        }
    }

    @PostMapping("/edit-seasonal-flight/{currentFlightCode}")
    public String editSeasonalFlight(@PathVariable String currentFlightCode,
                                     @ModelAttribute CreateSeasonalFlightRequest request,
                                     @RequestParam Map<String, String> allParams,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        AirlineCompany company = (AirlineCompany) session.getAttribute("loggedInCompany");
        if (company == null) return "redirect:/login";

        try {
            applySeatsAndPrices(request, allParams);
            request.setSeasonStart(MonthDay.parse("--" + allParams.get("seasonStartStr")));
            request.setSeasonEnd(MonthDay.parse("--" + allParams.get("seasonEndStr")));
            flightService.updateSeasonalFlight(company.getId(), currentFlightCode, request);
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/edit-flight/" + currentFlightCode;
        }
    }

    @PostMapping("/delete-flight/{flightCode}")
    public String deleteFlight(@PathVariable String flightCode, HttpSession session) {
        AirlineCompany company = (AirlineCompany) session.getAttribute("loggedInCompany");
        if (company == null) return "redirect:/login";
        
        flightService.removeFlight(company.getId(), flightCode);
        return "redirect:/dashboard";
    }

    private void applySeatsAndPrices(CreateRegularFlightRequest request, Map<String, String> allParams) {
        Map<ClassType, Integer> seats = parseSeats(allParams);
        Map<ClassType, Double> prices = parsePrices(allParams);
        request.setSeats(seats);
        request.setPrices(prices);
    }

    private void applySeatsAndPrices(CreateSeasonalFlightRequest request, Map<String, String> allParams) {
        Map<ClassType, Integer> seats = parseSeats(allParams);
        Map<ClassType, Double> prices = parsePrices(allParams);
        request.setSeats(seats);
        request.setPrices(prices);
    }

    private Map<ClassType, Integer> parseSeats(Map<String, String> allParams) {
        Map<ClassType, Integer> seats = new HashMap<>();
        seats.put(ClassType.BUSINESS, Integer.parseInt(allParams.get("seats_BUSINESS")));
        seats.put(ClassType.FIRST_CLASS, Integer.parseInt(allParams.get("seats_FIRST")));
        seats.put(ClassType.ECONOMY, Integer.parseInt(allParams.get("seats_ECONOMY")));
        return seats;
    }

    private Map<ClassType, Double> parsePrices(Map<String, String> allParams) {
        Map<ClassType, Double> prices = new HashMap<>();
        prices.put(ClassType.BUSINESS, Double.parseDouble(allParams.get("prices_BUSINESS")));
        prices.put(ClassType.FIRST_CLASS, Double.parseDouble(allParams.get("prices_FIRST")));
        prices.put(ClassType.ECONOMY, Double.parseDouble(allParams.get("prices_ECONOMY")));
        return prices;
    }

    private void addSeatAndPriceAttributes(Model model, Flight flight) {
        model.addAttribute("seatsBusiness", flight.getSeats().get(ClassType.BUSINESS));
        model.addAttribute("seatsFirst", flight.getSeats().get(ClassType.FIRST_CLASS));
        model.addAttribute("seatsEconomy", flight.getSeats().get(ClassType.ECONOMY));
        model.addAttribute("pricesBusiness", flight.getPrices().get(ClassType.BUSINESS));
        model.addAttribute("pricesFirst", flight.getPrices().get(ClassType.FIRST_CLASS));
        model.addAttribute("pricesEconomy", flight.getPrices().get(ClassType.ECONOMY));
    }

    private void addBookingFormAttributes(Model model, BookingRequest request) {
        model.addAttribute("outboundFlight", flightService.findDtoById(request.getOutboundFlightId(), request.getOutboundDate()));
        model.addAttribute("returnFlight", request.getReturnFlightId() == null ? null
                : flightService.findDtoById(request.getReturnFlightId(), request.getReturnDate()));
        model.addAttribute("classTypes", ClassType.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
    }

    private String formatMonthDay(MonthDay monthDay) {
        return monthDay == null ? "" : "%02d-%02d".formatted(monthDay.getMonthValue(), monthDay.getDayOfMonth());
    }
}
