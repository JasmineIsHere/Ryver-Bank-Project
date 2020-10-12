package test.java.ryver.app;

import ryver.app.customer.*;
import ryver.app.account.*;
import ryver.app.customer.Customer.*;
import ryver.app.account.Account.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ContentServiceTest {
    @Mock
    private ContentRepository contents;
    // findByCustomerId(Long customerId);
    // findByIdAndCustomerId(Long accountId, Long customerId);

    @Mock
    BCryptPasswordEncoder encoder;

    @InjectMocks
    private ContentController ContentController;

    void addContent_approveContent_returnSavedContent(){}
    void updateContent_ROLEManager_approveContentAndReturnSavedContent(){}
    void updateContent_ROLEAnalyst_returnSavedContent(){}
    void updateContent_invalidContentId_returnException(){}
    // public Content addContent(@Valid @RequestBody Content content) {
    //     public Content updateContent(@PathVariable (value = "contentId") Long contentId, @Valid @RequestBody Content updatedContentInfo) {
    //         public void deleteContent(@PathVariable (value = "contentId") Long contentId) {



}