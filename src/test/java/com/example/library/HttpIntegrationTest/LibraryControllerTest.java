package com.example.library.HttpIntegrationTest;

import com.example.library.component.JwtAuthenticationFilter;
import com.example.library.controller.LibraryController;
import com.example.library.dto.LibraryDTO;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.Library;
import com.example.library.model.LibraryStatus;
import com.example.library.service.JwtService;
import com.example.library.service.LibraryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LibraryController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "admin", roles = "ADMIN")
class LibraryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    LibraryService libraryService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    private Library buildLibrary(long id, String name, String address, LibraryStatus status) {
        return Library.builder()
                .id(id)
                .name(name)
                .address(address)
                .status(status)
                .build();
    }

    /* ---------- GET /libraries ---------- */

    @Test
    void getAllLibraries_ok() throws Exception {
        when(libraryService.getAllLibraries())
                .thenReturn(List.of(buildLibrary(1L, "Central", "Main St", LibraryStatus.ACTIVE)));

        mockMvc.perform(get("/libraries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllLibraries_noContent() throws Exception {
        when(libraryService.getAllLibraries()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/libraries"))
                .andExpect(status().isNoContent());
    }

    /* ---------- GET /libraries/{id} ---------- */

    @Test
    void getLibraryById_ok() throws Exception {
        when(libraryService.getLibraryById(6L))
                .thenReturn(buildLibrary(6L, "Branch", "Elm St", LibraryStatus.ACTIVE));

        mockMvc.perform(get("/libraries/6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6));
    }

    @Test
    void getLibraryById_notFound() throws Exception {
        when(libraryService.getLibraryById(6L))
                .thenThrow(new NotFoundException("Library with ID 6 not found"));

        mockMvc.perform(get("/libraries/6"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Library with ID 6 not found"));
    }

    /* ---------- GET /libraries/me ---------- */

    @Test
    void getMyLibrary_librarianRole_ok() throws Exception {
        when(libraryService.getLibraryForCurrentLibrarian())
                .thenReturn(buildLibrary(2L, "MyLib", "Oak St", LibraryStatus.ACTIVE));

        mockMvc.perform(get("/libraries/me")
                        .with(user("librarian").roles("LIBRARIAN")))   // override role
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("MyLib"));
    }

    /* ---------- POST /libraries ---------- */

    @Test
    void addLibrary_ok() throws Exception {
        LibraryDTO dto = new LibraryDTO("New Lib", "River Rd");
        mockMvc.perform(post("/libraries")
                        .contentType("application/json")
                        .content("""
                                 {"name":"New Lib","address":"River Rd"}"""))
                .andExpect(status().isOk());
        verify(libraryService).addLibrary(any(LibraryDTO.class));
    }

    @Test
    void addLibrary_duplicate_badRequest() throws Exception {
        doThrow(new BadRequestException("This library already exists"))
                .when(libraryService).addLibrary(any(LibraryDTO.class));

        mockMvc.perform(post("/libraries")
                        .contentType("application/json")
                        .content("""
                                 {"name":"Central","address":"Main St"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("This library already exists"));
    }

    /* ---------- DELETE /libraries/{id} ---------- */

    @Test
    void deleteLibrary_deleted_ok() throws Exception {
        when(libraryService.deleteLibrary(12L)).thenReturn(true);

        mockMvc.perform(delete("/libraries/12"))
                .andExpect(status().isOk())
                .andExpect(content().string("Library deleted successfully"));
    }

    @Test
    void deleteLibrary_closed_ok() throws Exception {
        when(libraryService.deleteLibrary(12L)).thenReturn(false);

        mockMvc.perform(delete("/libraries/12"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Library could not be deleted due to active loans or reservations. Status set to CLOSED."));
    }

    @Test
    void deleteLibrary_notFound() throws Exception {
        doThrow(new NotFoundException("Library with ID 12 doesn't exist."))
                .when(libraryService).deleteLibrary(12L);

        mockMvc.perform(delete("/libraries/12"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Library with ID 12 doesn't exist."));
    }

    /* ---------- PUT /libraries/{id} ---------- */

    @Test
    void updateLibrary_ok() throws Exception {
        mockMvc.perform(put("/libraries/5")
                        .contentType("application/json")
                        .content("""
                                 {"name":"Updated","address":"New Addr"}"""))
                .andExpect(status().isOk())
                .andExpect(content().string("Library updated successfully"));

        verify(libraryService).updateLibrary(eq(5L), any(LibraryDTO.class));
    }

    @Test
    void updateLibrary_badRequest() throws Exception {
        doThrow(new BadRequestException("At least one field (name or address) must be provided for update."))
                .when(libraryService).updateLibrary(eq(5L), any(LibraryDTO.class));

        mockMvc.perform(put("/libraries/5")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "At least one field (name or address) must be provided for update."));
    }

    @Test
    void updateLibrary_notFound() throws Exception {
        doThrow(new NotFoundException("Library with ID 5 does not exist"))
                .when(libraryService).updateLibrary(eq(5L), any(LibraryDTO.class));

        mockMvc.perform(put("/libraries/5")
                        .contentType("application/json")
                        .content("""
                                 {"name":"X"}"""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Library with ID 5 does not exist"));
    }

    /* ---------- GET /libraries/search ---------- */

    @Test
    void searchLibraries_results_ok() throws Exception {
        when(libraryService.searchLibraries("Central", null))
                .thenReturn(List.of(buildLibrary(1L, "Central", "Main St", LibraryStatus.ACTIVE)));

        mockMvc.perform(get("/libraries/search").param("name", "Central"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Central"));
    }

    @Test
    void searchLibraries_noContent() throws Exception {
        when(libraryService.searchLibraries(null, "Unknown")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/libraries/search").param("address", "Unknown"))
                .andExpect(status().isNoContent());
    }
}
