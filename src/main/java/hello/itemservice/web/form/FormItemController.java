package hello.itemservice.web.form;

import hello.itemservice.domain.item.DeliveryCode;
import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.ItemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/form/items")
@RequiredArgsConstructor
public class FormItemController {

    private final ItemRepository itemRepository;

    /**
     * <@ModelAttribute의 특별한 사용법>
     * 이렇게 @ModelAttribute를 컨트롤러 내의 별도 메서드에 적용하면
     * 해당 컨트롤러를 요청할 때 regions에서 반환한 값이 자동으로 모델(model)에 담기게 됨
     * 단, 컨트롤러 내 메서드가 호출될 때마다 해당 객체가 계속 생성되므로
     * 다른 곳에서 미리 한번만 생성해놓고 재사용하는 것이 바람직함
     */
    @ModelAttribute("regions")
    public Map<String, String> regions() {
        Map<String, String> regions = new LinkedHashMap<>();
        regions.put("SEOUL", "서울");
        regions.put("BUSAN", "부산");
        regions.put("JEJU", "제주");
        return regions;
    }

    @ModelAttribute("itemTypes")
    public ItemType[] itemTypes() {
        return ItemType.values(); // Enum 클래스의 .values() 메서드는 해당 Enum 클래스의 모든 정보를 배열로 반환
    }

    @ModelAttribute("deliveryCodes")
    public List<DeliveryCode> deliveryCodes() {
        List<DeliveryCode> deliveryCodes = new ArrayList<>();
        deliveryCodes.add(new DeliveryCode("FAST", "빠른배송"));
        deliveryCodes.add(new DeliveryCode("NORMAL", "일반배송"));
        deliveryCodes.add(new DeliveryCode("SLOW", "느린배송"));
        return deliveryCodes;
    }

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "form/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "form/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "form/addForm";
    }

    /**
     *  BindingResult 파라미터는 반드시 @ModelAttribute 어노테이션 파라미터 바로 뒤에 위치해야 함!
     *
     *  <스프링의 바인딩 오류 처리>
     * 타입 오류로 바인딩에 실패하면 스프링은 FieldError 를 생성하면서 사용자가 입력한 값을 넣어둠.
     * 그리고 해당 오류를 BindingResult 에 담아서 컨트롤러를 호출.
     * 따라서 타입 오류 같은 바인딩 실패시에도 사용자가 잘못 입력한 값을 그대로 정상적으로 출력 가능
     *  */
    @PostMapping("/add")
    public String addItem(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        /**
         * RejectValue, Reject 메서드 파라미터
         * field : 오류 필드명
         * errorCode : 오류 코드(이 오류 코드는 메시지에 등록된 코드가 아니다. 뒤에서 설명할
         * messageResolver를 위한 오류 코드이다.)
         * errorArgs : 오류 메시지에서 {0} 을 치환하기 위한 값
         * defaultMessage : 오류 메시지를 찾을 수 없을 때 사용하는 기본 메시지
         */

        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.rejectValue("itemName", "required");
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.rejectValue("price", "range", new Object[] {1000, 1000000}, null);
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        //특정 필드 예외가 아닌 전체 예외
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        if (bindingResult.hasErrors()) {
            log.info("error={}", bindingResult);
            return "form/addForm";
        }

        log.info("item.open={}", item.getOpen());
        log.info("item.regions={}", item.getRegions());
        log.info("item.itemTypes={}", item.getItemType());

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/form/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "form/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/form/items/{itemId}";
    }

}

