    package com.example.fullstack.database.controller;

    import com.example.fullstack.database.dto.MenuItemDTO;
    import com.example.fullstack.database.dto.service.implementation.MenuItemDTOServiceImpl;
    import com.example.fullstack.database.model.MenuItem;
    import com.example.fullstack.database.model.MenuItemCategory;
    import com.example.fullstack.database.model.Size;
    import com.example.fullstack.database.service.implementation.MenuItemServiceImpl;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/api/menuItem")
    public class MenuItemController {

        MenuItemServiceImpl menuItemServiceImpl;
        private final MenuItemDTOServiceImpl menuItemDTOServiceImpl;
        public MenuItemController(MenuItemServiceImpl menuItemServiceImpl, MenuItemDTOServiceImpl menuItemDTOServiceImpl) {
            this.menuItemServiceImpl = menuItemServiceImpl;
            this.menuItemDTOServiceImpl = menuItemDTOServiceImpl;
        }

        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping("/add")
        public String addMenuItems(@RequestBody MenuItem menuItem) {
            menuItemServiceImpl.addMenuItem(menuItem);
            return "Success";
        }

        @GetMapping("/get/{id}")
        public MenuItem getMenuItemById(@PathVariable String id) {
            return menuItemServiceImpl.getMenuItemById(id) .orElseThrow(() -> new RuntimeException("MenuItem not found with ID: " + id));
        }

        @PutMapping ("/update/{menuItemId}")
        public ResponseEntity<?> updateMenuItem(@PathVariable String menuItemId, @RequestBody MenuItem menuItem) {
            try{
                menuItemServiceImpl.updateMenuItem(menuItemId, menuItem);
                return ResponseEntity.ok("Menuitem with id " + menuItemId + " updated successfully");
            }catch (RuntimeException e){
                return ResponseEntity.ok("Menuitem with id " + menuItemId + " update failed");
            }
        }

        @PostMapping("/add-all")
        public String addMenuItems(@RequestBody List<MenuItem> menuItems) {
            menuItemServiceImpl.addAllMenuItems(menuItems);
            return "Success";
        }

        @GetMapping("/items")
        public List<MenuItemDTO> getMenuItemsByCategoryAndSize(
                @RequestParam MenuItemCategory category,
                @RequestParam Size size) {
            return menuItemServiceImpl.getMenuItemsByCategoryAndSize(category, size)
                    .stream()
                    .map(menuItemDTOServiceImpl::convertToDTO)
                    .toList();
        }


        @GetMapping("/get/category/{category}")
        public List<MenuItemDTO> getMenuItemsByCategory(@PathVariable MenuItemCategory category) {
            return menuItemServiceImpl.getMenuItemsByCategory(category)
                    .stream()
                    .map(menuItemDTOServiceImpl::convertToDTO)
                    .toList();
        }


        @DeleteMapping("/delete/all")
        public String deleteAllMenuItems() {
            menuItemServiceImpl.deleteAllMenuItems();
            return "Deleted all menu items";
        }

        @GetMapping("/get/name/size")
        public List<MenuItemDTO> getMenuItemsByName(@RequestParam String name, @RequestParam Size size) {
            return menuItemServiceImpl.getMenuItemByNameAndSize(name, size)
                    .stream()
                    .map(menuItemDTOServiceImpl::convertToDTO)
                    .toList();
        }

    }
