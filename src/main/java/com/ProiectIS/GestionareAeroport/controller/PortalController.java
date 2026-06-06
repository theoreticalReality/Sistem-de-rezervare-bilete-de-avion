package com.ProiectIS.GestionareAeroport.controller;

import com.ProiectIS.GestionareAeroport.dto.AirlineLoginRequest;
import com.ProiectIS.GestionareAeroport.dto.AirlineRegistrationRequest;
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
import com.ProiectIS.GestionareAeroport.model.DiscountPolicy;
import com.ProiectIS.GestionareAeroport.model.Flight;
import com.ProiectIS.GestionareAeroport.model.RegularFlight;
import com.ProiectIS.GestionareAeroport.model.SeasonalFlight;
import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import com.ProiectIS.GestionareAeroport.model.enums.PaymentMethod;
import com.ProiectIS.GestionareAeroport.repository.AirportStaffRepository;
import com.ProiectIS.GestionareAeroport.service.AirlineCompanyService;
import com.ProiectIS.GestionareAeroport.service.BookingService;
import com.ProiectIS.GestionareAeroport.service.DiscountPolicyService;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PortalController {

    private final AirlineCompanyService airlineService;
    private final FlightService flightService;
    private final FlightSearchService flightSearchService;
    private final BookingService bookingService;
    private final AirportStaffRepository staffRepository;
    private final DiscountPolicyService discountPolicyService;

    public PortalController(AirlineCompanyService airlineService,
                            FlightService flightService,
                            FlightSearchService flightSearchService,
                            BookingService bookingService,
                            AirportStaffRepository staffRepository,
                            DiscountPolicyService discountPolicyService) {
        this.airlineService = airlineService;
        this.flightService = flightService;
        this.flightSearchService = flightSearchService;
        this.bookingService = bookingService;
        this.staffRepository = staffRepository;
        this.discountPolicyService = discountPolicyService;
    }

    @GetMapping("/api/flights/cities/departure")
    @ResponseBody
    public List<String> getDepartureCities() {
        return flightSearchService.getAvailableDepartureCities();
    }

    @GetMapping("/api/flights/cities/destination")
    @ResponseBody
    public List<String> getDestinations(@RequestParam String from) {
        return flightSearchService.getAvailableDestinations(from);
    }

    @GetMapping("/api/flights/available-dates")
    @ResponseBody
    public List<LocalDate> getAvailableDates(@RequestParam String from, @RequestParam String to) {
        return flightSearchService.getAvailableDates(from, to);
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

    @GetMapping("/bookings/lookup")
    public String lookupBooking(@RequestParam String bookingId, Model model, RedirectAttributes redirectAttributes) {
        try {
            BookingResponse booking = bookingService.findResponseByBookingId(bookingId.trim());
            model.addAttribute("booking", booking);
            return "booking-confirmation";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Nu am gasit nicio rezervare cu codul: " + bookingId);
            return "redirect:/";
        }
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new AirlineLoginRequest());
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registrationRequest", new AirlineRegistrationRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute AirlineRegistrationRequest registrationRequest, RedirectAttributes redirectAttributes) {
        try {
            airlineService.register(registrationRequest);
            redirectAttributes.addFlashAttribute("success", "Contul a fost creat cu succes! Te poti loga acum.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @PostMapping("/login")
    public String login(@ModelAttribute AirlineLoginRequest loginRequest, HttpSession session, RedirectAttributes redirectAttributes) {
        System.out.println("Tentativa de login pentru: " + loginRequest.getEmail());
        try {
            AirlineCompany company = airlineService.login(loginRequest.getEmail(), loginRequest.getPassword());
            System.out.println("Login reusit pentru: " + company.getName());
            session.setAttribute("loggedInCompany", company);
            return "redirect:/dashboard";
        } catch (Exception e) {
            System.out.println("Login esuat: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Email/ID companie sau parola incorecte.");
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

    @GetMapping("/staff/register")
    public String staffRegisterPage() {
        return "staff-register";
    }

    @PostMapping("/staff/register")
    public String staffRegister(@RequestParam String name, 
                                @RequestParam String personalCode, 
                                RedirectAttributes redirectAttributes) {
        try {
            if (staffRepository.findByPersonalCode(personalCode).isPresent()) {
                throw new IllegalArgumentException("Acest cod personal este deja folosit.");
            }
            AirportStaff staff = new AirportStaff();
            staff.setName(name);
            staff.setPersonalCode(personalCode);
            staff.setStaffId("STAFF-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            staffRepository.save(staff);
            redirectAttributes.addFlashAttribute("success", "Codul personal a fost creat! Te poti loga acum.");
            return "redirect:/staff/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/staff/register";
        }
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
                                 @RequestParam(required = false) String bookingId,
                                 HttpSession session,
                                 Model model) {
        AirportStaff staff = (AirportStaff) session.getAttribute("loggedInStaff");
        if (staff == null) {
            return "redirect:/staff/login";
        }

        String normalizedFlightCode = flightCode == null ? "" : flightCode.trim();
        String normalizedBookingId = bookingId == null ? "" : bookingId.trim();

        model.addAttribute("staff", staff);
        model.addAttribute("selectedFlightCode", normalizedFlightCode);
        model.addAttribute("selectedBookingId", normalizedBookingId);
        model.addAttribute("flights", flightService.findAllDtos());

        if (!normalizedBookingId.isBlank()) {
            try {
                BookingResponse booking = bookingService.findResponseByBookingId(normalizedBookingId);
                model.addAttribute("bookings", List.of(booking));
                model.addAttribute("selectedFlightCode", ""); 
            } catch (Exception e) {
                model.addAttribute("error", "Nu am gasit nicio rezervare cu ID-ul: " + normalizedBookingId);
                model.addAttribute("bookings", List.of());
            }
        }
 else if (!normalizedFlightCode.isBlank()) {
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
            redirectAttributes.addFlashAttribute("success", "Plata cash a fost validata.");
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
        model.addAttribute("bookings", bookingService.findResponsesByAirline(company.getId()));
        return "dashboard";
    }

    @GetMapping("/flights/{flightCode}/occupancy")
    public String flightOccupancy(@PathVariable String flightCode, HttpSession session, Model model) {
        AirlineCompany company = (AirlineCompany) session.getAttribute("loggedInCompany");
        if (company == null) {
            return "redirect:/login";
        }
        try {
            model.addAttribute("company", company);
            model.addAttribute("stats", flightService.getFlightOccupancy(company.getId(), flightCode));
            return "flight-occupancy";
        } catch (Exception e) {
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/discount-policy")
    public String discountPolicyPage(HttpSession session, Model model) {
        if (session.getAttribute("loggedInCompany") == null) {
            return "redirect:/login";
        }
        DiscountPolicy policy = discountPolicyService.getPolicy();
        model.addAttribute("roundTripDiscountPercent", policy.getRoundTripDiscount());
        model.addAttribute("lastMinuteDiscountPercent", policy.getLastMinuteDiscount());
        model.addAttribute("defaultRoundTrip", DiscountPolicy.DEFAULT_ROUND_TRIP_DISCOUNT);
        model.addAttribute("defaultLastMinute", DiscountPolicy.DEFAULT_LAST_MINUTE_DISCOUNT);
        return "discount-policy";
    }

    @PostMapping("/discount-policy")
    public String updateDiscountPolicy(@RequestParam(required = false) Double roundTripDiscountPercent,
                                       @RequestParam(required = false) Double lastMinuteDiscountPercent,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInCompany") == null) {
            return "redirect:/login";
        }
        Double roundTrip = roundTripDiscountPercent == null
                ? DiscountPolicy.DEFAULT_ROUND_TRIP_DISCOUNT : roundTripDiscountPercent;
        Double lastMinute = lastMinuteDiscountPercent == null
                ? DiscountPolicy.DEFAULT_LAST_MINUTE_DISCOUNT : lastMinuteDiscountPercent;
        try {
            if (roundTrip < 0 || roundTrip > 100 || lastMinute < 0 || lastMinute > 100) {
                throw new IllegalArgumentException("Procentele discount-urilor trebuie sa fie intre 0 si 100.");
            }
            discountPolicyService.updatePolicy(roundTrip, lastMinute);
            redirectAttributes.addFlashAttribute("success", "Politica de discount a fost actualizata.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/discount-policy";
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
                redirectAttributes.addFlashAttribute("error", "Nu poti edita un zbor care nu iti apartine.");
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
    public String deleteFlight(@PathVariable String flightCode,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        AirlineCompany company = (AirlineCompany) session.getAttribute("loggedInCompany");
        if (company == null) return "redirect:/login";

        try {
            flightService.removeFlight(company.getId(), flightCode);
            redirectAttributes.addFlashAttribute("success", "Operatiunea a fost aplicata. Daca zborul avea rezervari, a fost anulat si pastrat in istoric.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
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
        var outboundFlight = flightService.findDtoById(request.getOutboundFlightId(), request.getOutboundDate());
        var returnFlight = request.getReturnFlightId() == null ? null
                : flightService.findDtoById(request.getReturnFlightId(), request.getReturnDate());
        DiscountPolicy policy = discountPolicyService.getPolicy();

        model.addAttribute("outboundFlight", outboundFlight);
        model.addAttribute("returnFlight", returnFlight);
        model.addAttribute("classTypes", ClassType.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("roundTripDiscountPercent", policy.getRoundTripDiscount());
        model.addAttribute("lastMinuteDiscountPercent", policy.getLastMinuteDiscount());
        model.addAttribute("lastMinuteDiscountApplies", policy.isLastMinute(outboundFlight.getDepartureDateTime()));
    }

    private String formatMonthDay(MonthDay monthDay) {
        return monthDay == null ? "" : "%02d-%02d".formatted(monthDay.getMonthValue(), monthDay.getDayOfMonth());
    }
}
