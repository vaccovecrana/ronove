package io.vacco.ronove;

import io.vacco.ronove.core.RvAttachmentParam;
import io.vacco.ronove.core.RvException;
import io.vacco.ronove.core.RvStatus;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.*;

public class MyBookApi {

  public static final String v1BookCatalogIdList = "/v1/book/{catalogId}/list";
  public static final String v1BookGenreList = "/v1/book/genre/list";
  public static final String v1BookGenre = "/v1/book/genre";
  public static final String v1BookCatalog = "/v1/book/catalog";
  public static final String v1GenreUpdate = "/v1/genre/update";
  public static final String v1Me = "/v1/me";

  public String[] genres = {"suspense", "fiction", "classic"};

  public static String[] bookTitles = {
      "Absalom, Absalom!",
      "A Time to Kill by John Grisham.",
      "The House of Mirth by Edith Wharton.",
      "East of Eden by John Steinbeck.",
      "The Sun Also Rises by Ernest Hemingway.",
      "Moab is my Washpot by Stephen Fry."
  };

  @GET @Path(v1BookCatalogIdList)
  public List<String> v1BookCatalogIdList(@PathParam("catalogId") int catalogId) {
    return Arrays.asList(bookTitles);
  }

  @GET @Path(v1BookGenreList)
  public String[] v1BookGenreList() {
    return genres;
  }

  @GET @Path(v1BookGenre)
  public List<String> v1BookGenre(@QueryParam("genre") String genre,
                                  @QueryParam("sort") @DefaultValue("asc") String sort) {
    return Arrays.asList(bookTitles);
  }

  @POST @Path(v1BookGenre)
  public List<String> v1BookGenreUpdateTitles(@QueryParam("genre") String genre, @QueryParam("titleCount") int titleCount) {
    return Arrays.asList(bookTitles);
  }

  @GET @Path(v1BookCatalog)
  public List<String> v1BookCatalog(@HeaderParam("catalogIds") List<Integer> catalogIds) {
    return Arrays.asList(bookTitles);
  }

  @PATCH @Path(v1GenreUpdate)
  @RvStatus(Response.Status.NO_CONTENT)
  public void v1GenreUpdate(@BeanParam List<String> genreUpdates) {
    throw new RvException.RvApplicationException(
        new IllegalStateException("This error should be handled by a blocking customizer."),
        Response.Status.BAD_REQUEST, new int[] {1, 2, 3, 4, 5}
    );
  }

  @GET @Path(v1Me)
  public MyUser v1Me(@QueryParam("includeAvatar") boolean includeAvatar,
                     @RvAttachmentParam(MyUser.class) MyUser sessionUser) {
    if (!includeAvatar) {
      sessionUser.avatarUrl = null;
    }
    return sessionUser;
  }

}

