package com.ProiectIS.GestionareAeroport.controller;

import com.ProiectIS.GestionareAeroport.dto.AirlineLoginRequest;
import com.ProiectIS.GestionareAeroport.dto.CreateRegularFlightRequest;
import com.ProiectIS.GestionareAeroport.dto.CreateSeasonalFlightRequest;
import com.ProiectIS.GestionareAeroport.model.AirlineCompany;
import com.ProiectIS.GestionareAeroport.model.Flight;
import com.ProiectIS.GestionareAeroport.model.RegularFlight;
import com.ProiectIS.GestionareAeroport.model.SeasonalFlight;
import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import com.ProiectIS.GestionareAeroport.service.AirlineCompanyService;
import com.ProiectIS.GestionareAeroport.service.FlightService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.MonthDay;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PortalController {

    private final AirlineCompanyService airlineService;
    private final FlightService flightService;

    public PortalController(AirlineCompanyService airlineService, FlightService flightService) {
        this.airlineService = airlineService;
        this.flightService = flightService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
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
        return "redirect:/login";
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
            } else if (flight instanceof SeasonalFlight seasonalFlight) {
                model.addAttribute("daysOfWeek", seasonalFlight.getDaysOfWeek());
                model.addAttribute("departureTime", seasonalFlight.getDepartureTime());
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

    private String formatMonthDay(MonthDay monthDay) {
        return monthDay == null ? "" : "%02d-%02d".formatted(monthDay.getMonthValue(), monthDay.getDayOfMonth());
    }
}
