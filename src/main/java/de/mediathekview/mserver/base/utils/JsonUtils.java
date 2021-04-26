package de.mediathekview.mserver.base.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

import java.util.Arrays;
import java.util.Optional;

/**
 * A util class to collect useful Json related methods.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *     <b>Mail:</b> nicklas@wiegandt.eu<br>
 *     <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 */
public final class JsonUtils {
  private JsonUtils() {
    super();
  }

  /**
   * Checks if a JSON tree path outgoing from the first JsonElement is correct and all given element
   * IDs exist in the given order.<br>
   * Example: If the tree is MainObj {@literal -> } FirstChild {@literal -> } SecondChild and the
   * given JsonElement is MainObj and the element IDs are "FirstChild,SecondChild" this will return
   * true.
   *
   * @param aJsonElement The first element from which the tree will be checked.
   * @param aCrawler The crawler which using this. If it's given and a element is missing the {@link
   *     AbstractCrawler#printMissingElementErrorMessage(String)} will be called for it.
   * @param aElementIds The tree-childs in the order in which they should be checked.
   * @return true if the JSON tree path is correct and all given element IDs exist in the given
   *     order.
   */
  public static boolean checkTreePath(
      final JsonElement aJsonElement,
      final Optional<AbstractCrawler> aCrawler,
      final String... aElementIds) {
    JsonElement elemToCheck = aJsonElement;
    for (final String elementId : aElementIds) {
      if (elemToCheck.isJsonObject() && elemToCheck.getAsJsonObject().has(elementId)) {
        elemToCheck = elemToCheck.getAsJsonObject().get(elementId);
      } else {
        aCrawler.ifPresent(
            abstractCrawler -> abstractCrawler.printMissingElementErrorMessage(elementId));
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the value of an attribute
   *
   * @param aJsonObject the object
   * @param aAttributeName the name of the attribute
   * @return the value of the attribute, if it exists, else Optional.empty
   */
  public static Optional<String> getAttributeAsString(
      final JsonObject aJsonObject, final String aAttributeName) {
    if (aJsonObject.has(aAttributeName)) {
      final JsonElement aElement = aJsonObject.get(aAttributeName);
      if (!aElement.isJsonNull()) {
        return Optional.of(aElement.getAsString());
      }
    }

    return Optional.empty();
  }

  public static Optional<String> getElementValueAsString(final JsonElement aJsonElement, final String... aElementIds) {
    Optional<String> rs = Optional.empty();
    try {
      JsonObject aJsonObject = aJsonElement.getAsJsonObject();
      for (int i = 0; i < aElementIds.length-1; i++) {
        String elementId = aElementIds[i];
        if (aJsonObject.has(elementId) && aJsonObject.get(elementId).isJsonObject()) {
          aJsonObject = aJsonObject.getAsJsonObject(elementId);
        } else {
          aJsonObject = null;
          break;
        }
      }
      //
      String elementId = aElementIds[aElementIds.length-1];
      if (aJsonObject != null && aJsonObject.has(elementId) && !aJsonObject.get(elementId).isJsonNull()) {
        rs =  Optional.of(aJsonObject.get(elementId).getAsString());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    //
    return rs;
  }
  
  /**
   * Checks if the {@link JsonElement} is a {@link JsonObject} and if it has all given elements and
   * if no element is null.
   *
   * @param aJsonElement The element to check.
   * @param aCrawler The crawler which runs this task. When given and a given element is missing the
   *     error counter will be increased and a debug message will be printed.
   * @param aElementIds The elements which it should has.
   * @return true when the element is a {@link JsonObject} and if it has all given elements and if
   *     no element is null.
   */
  public static boolean hasElements(
      final JsonElement aJsonElement,
      final Optional<? extends AbstractCrawler> aCrawler,
      final String... aElementIds) {
    return aJsonElement.isJsonObject()
        && hasElements(aJsonElement.getAsJsonObject(), aCrawler, aElementIds);
  }

  /**
   * Checks if the {@link JsonElement} is a {@link JsonObject} and if it has all given elements and
   * if no element is null.
   *
   * @param aJsonElement The element to check.
   * @param aElementIds The elements which it should has.
   * @return true when the element is a {@link JsonObject} and if it has all given elements and if
   *     no element is null.
   */
  public static boolean hasElements(final JsonElement aJsonElement, final String... aElementIds) {
    return hasElements(aJsonElement, Optional.empty(), aElementIds);
  }

  /**
   * Checks if the {@link JsonObject} has all given elements and if no element is null.
   *
   * @param aJsonObject The object to check.
   * @param aCrawler The crawler which runs this task. When given and a given element is missing the
   *     error counter will be increased and a debug message will be printed.
   * @param aElementIds The elements which it should has.
   * @return true when the object has all given elements and if no element is null.
   */
  public static boolean hasElements(
      final JsonObject aJsonObject,
      final Optional<? extends AbstractCrawler> aCrawler,
      final String... aElementIds) {
    for (final String elementId : aElementIds) {
      if (!aJsonObject.has(elementId) || aJsonObject.get(elementId).isJsonNull()) {
        if (aCrawler.isPresent()) {
          aCrawler.get().printMissingElementErrorMessage(elementId);
        }
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if the {@link JsonObject} has all given elements and if no element is null or empty.
   *
   * @param aJsonObject The object to check.
   * @param aCrawler The crawler which runs this task. When given and a given element is missing the
   *     error counter will be increased and a debug message will be printed.
   * @param aElementIds The elements which it should has.
   * @return true when the object has all given elements and if no element is null.
   */
  public static boolean hasStringElements(
      final JsonObject aJsonObject,
      final Optional<? extends AbstractCrawler> aCrawler,
      final String... aElementIds) {
    return hasElements(aJsonObject, aCrawler, aElementIds)
        && Arrays.stream(aElementIds)
            .map(aJsonObject::get)
            .map(JsonElement::getAsString)
            .noneMatch(String::isEmpty);
  }

  /**
   * Checks if the {@link JsonObject} has all given elements and if no element is null.
   *
   * @param aJsonObject The object to check.
   * @param aElementIds The elements which it should has.
   * @return true when the object has all given elements and if no element is null.
   */
  public static boolean hasElements(final JsonObject aJsonObject, final String... aElementIds) {
    return hasElements(aJsonObject, Optional.empty(), aElementIds);
  }
}
