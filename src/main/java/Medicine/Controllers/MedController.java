package Medicine.Controllers;

import Medicine.DAO.MedDAOImpl;
import Medicine.Entities.Medicament;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@ComponentScan("Medicine")
public class MedController {
    private final MedDAOImpl medDAO;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public MedController(MedDAOImpl medDAO, RabbitTemplate rabbitTemplate) {
        this.medDAO = medDAO;
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/")
    public String mainMenu() {
        return "Menu";
    }

    @GetMapping("/show")
    public String show(Model model) {
        model.addAttribute("meds", medDAO.getMedList());
        return "Show";
    }

    @PostMapping("/show")
    public String buyMed(@RequestParam(value = "id") int id) {
        if (medDAO.getMedByID(id) == null) {
            return "redirect:/";
        }
        rabbitTemplate.convertAndSend("medQueue", "ID " + id + " med ordered");
        return "redirect:/show";
    }

    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("med", new Medicament());
        return "Add";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("med") Medicament med, BindingResult result) {
        if (result.hasErrors()) {
            return "Add";
        }
        medDAO.addMed(med);
        rabbitTemplate.convertAndSend("medQueue", med.toString() + " added");
        return "redirect:/";
    }

    @GetMapping("/remove")
    public String remove() {
        return "Remove";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam(value = "id") int id, Model model) {
        if (medDAO.getMedByID(id) == null) {
            String message = "There is no such medicament";
            model.addAttribute("message", message);
            return "Remove";
        }
        medDAO.removeMed(id);
        rabbitTemplate.convertAndSend("medQueue", "Medicine with ID " + id + " removed");
        return "redirect:/";
    }

    @GetMapping("/edit")
    public String editID() {
        return "Edit";
    }

    @PostMapping("/edit")
    public String editID(@RequestParam(value = "id") int id, Model model) {
        if (medDAO.getMedByID(id) == null) {
            String message = "There is no such medicament";
            model.addAttribute("message", message);
            return "Edit";
        }
        model.addAttribute(medDAO.getMedByID(id));
        return "redirect:/edit/" + id;
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable String id, Model model) {
        model.addAttribute("med", medDAO.getMedByID(Integer.parseInt(id)));
        return "EditMed";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable String id, @Valid @ModelAttribute("med") Medicament med, BindingResult result) {
        if (result.hasErrors()) {
            return "redirect:/edit/" + id;
        }
        medDAO.updateMed(Integer.parseInt(id),
                med.getName(),
                med.getType(),
                med.getForm(),
                med.getPrice(),
                med.getCount());
        rabbitTemplate.convertAndSend("medQueue", "Medicine with ID " + id + " edited");
        return "redirect:/";
    }

    @GetMapping("/info")
    public String showBiggestCount(Model model) {
        int count = medDAO.findBiggestCount();
        model.addAttribute("count", count);
        return "BiggestCount";
    }
}
