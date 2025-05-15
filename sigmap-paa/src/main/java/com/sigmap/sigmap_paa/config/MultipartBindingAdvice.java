//package com.sigmap.sigmap_paa.config;
//import org.springframework.web.bind.WebDataBinder;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.InitBinder;
//import org.springframework.beans.propertyeditors.CustomCollectionEditor;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.HashSet;
//import java.util.Set;
//@ControllerAdvice
//public class MultipartBindingAdvice {
//
//    /**
//     * Configures the WebDataBinder to properly handle nested collections.
//     * This helps with form-data binding for complex objects with nested collections.
//     *
//     * @param binder the WebDataBinder to configure
//     */
//    @InitBinder
//    public void initBinder(WebDataBinder binder) {
//        // Handle List type properties with proper initialization
//        binder.registerCustomEditor(List.class, new CustomCollectionEditor(List.class) {
//            @Override
//            protected Object convertElement(Object element) {
//                return super.convertElement(element);
//            }
//
//            @Override
//            public void setValue(Object value) {
//                if (value == null) {
//                    super.setValue(new ArrayList<>());
//                } else {
//                    super.setValue(value);
//                }
//            }
//        });
//
//        // Handle Set type properties with proper initialization
//        binder.registerCustomEditor(Set.class, new CustomCollectionEditor(Set.class) {
//            @Override
//            protected Object convertElement(Object element) {
//                return super.convertElement(element);
//            }
//
//            @Override
//            public void setValue(Object value) {
//                if (value == null) {
//                    super.setValue(new HashSet<>());
//                } else {
//                    super.setValue(value);
//                }
//            }
//        });
//    }
//}