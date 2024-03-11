package hello.itemservice.web.item;

import hello.itemservice.domain.item.DeliveryCode;
import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.ItemType;
import hello.itemservice.web.item.form.ItemSaveForm;
import hello.itemservice.web.item.form.ItemUpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
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
public class ItemController {

    private final ItemRepository itemRepository;

    /* BeanValidator 도입으로 인한 주석 처리 */
//    private final ItemValidator itemValidator;

//    @InitBinder
//    public void init(WebDataBinder webDataBinder) {
//        // WebDataBinder에 itemValidator 검증기 추가
//        webDataBinder.addValidators(itemValidator);
//    }

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
     * <스프링의 바인딩 오류 처리>
     * 타입 오류로 바인딩에 실패하면 스프링은 FieldError 를 생성하면서 사용자가 입력한 값을 넣어둠.
     * 그리고 해당 오류를 BindingResult 에 담아서 컨트롤러를 호출.
     * 따라서 타입 오류 같은 바인딩 실패시에도 사용자가 잘못 입력한 값을 그대로 정상적으로 출력 가능
     *  */
    @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute("item") ItemSaveForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        /**
         * Item 클래스에 대해 WebDateBinder에 등록된 검증기를 실행하라는 @Validated 애노테이션 추가하고
         * itemValidator.validate 메서드 호출 소스 부분은 주석 처리
         *
         * <2024-03-10 - BeanValidation 적용>
         * 스프링부트는 자동으로 `LocalValidatorFactoryBean`을 글로벌 Validator로 등록해줌
         * 이 Validator는 `@NotNull` 같은 애노테이션을 보고 검증을 수행
         * 이렇게 글로벌 Validator가 적용되어 있기 때문에, `@Valid` , `@Validated` 만 적용하면 됨.
         * 검증 오류가 발생하면, `FieldError` , `ObjectError` 를 생성해서 `BindingResult` 에 담아줌
         */
        // itemValidator.validate(item, bindingResult);

        //특정 필드 예외가 아닌 전체 예외
        if (form.getPrice() != null && form.getQuantity() != null) {
            int resultPrice = form.getPrice() * form.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        if (bindingResult.hasErrors()) {
            log.info("error={}", bindingResult);
            return "form/addForm";
        }

        Item item = new Item(
                form.getItemName(),
                form.getPrice(),
                form.getQuantity(),
                form.getOpen(),
                form.getRegions(),
                form.getItemType(),
                form.getDeliveryCode()
        );

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
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute("item") ItemUpdateForm form, BindingResult bindingResult) {

        //특정 필드 예외가 아닌 전체 예외
        if (form.getPrice() != null && form.getQuantity() != null) {
            int resultPrice = form.getPrice() * form.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "form/editForm";
        }

        Item item = new Item(
                form.getItemName(),
                form.getPrice(),
                form.getQuantity(),
                form.getOpen(),
                form.getRegions(),
                form.getItemType(),
                form.getDeliveryCode()
        );
        item.setId(form.getId());

        itemRepository.update(itemId, item);
        return "redirect:/form/items/{itemId}";
    }
}

