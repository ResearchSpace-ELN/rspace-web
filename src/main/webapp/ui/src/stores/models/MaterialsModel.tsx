import InvApiService from "../../common/InvApiService";
import * as ArrayUtils from "../../util/ArrayUtils";
import { toCommonUnit, fromCommonUnit } from "../definitions/Units";
import RsSet from "../../util/set";
import ContainerModel, { type ContainerAttrs } from "./ContainerModel";
import SubSampleModel, { type SubSampleAttrs } from "./SubSampleModel";
import { type SampleAttrs } from "./SampleModel";
import getRootStore from "../stores/RootStore";
import MemoisedFactory from "./Factory/MemoisedFactory";
import AlwaysNewFactory from "./Factory/AlwaysNewFactory";
import { mkAlert } from "../contexts/Alert";
import Search from "./Search";
import {
  action,
  observable,
  computed,
  makeObservable,
  runInAction,
} from "mobx";
import React from "react";
import { type InventoryRecord } from "../definitions/InventoryRecord";
import { GlobalId, type Id } from "../definitions/BaseRecord";
import { type ExportOptions } from "../definitions/Search";
import { type UnitCategory } from "../stores/UnitStore";
import { showToastWhilstPending } from "../../util/alerts";
import * as Parsers from "../../util/parsers";
import Result from "../../util/result";
import { getErrorMessage } from "@/util/error";
import { filterForThoseWithLocations, hasLocation } from "./HasLocation";
import { HasLocation } from "../definitions/HasLocation";
import { filterForThoseWithQuantities, hasQuantity } from "./HasQuantity";

export type ListOfMaterialsId = number | null;
export type ElnFieldId = number;
export type ElnDocumentId = number;

export type Quantity = {
  unitId: number;
  numericValue: number;
};

export type MaterialAttrs = {
  invRec: InventoryRecord;
  usedQuantity: Quantity | null;
};

type MaterialBackendParams = {
  invRec: {
    id: number | null;
    type: string;
  };
  usedQuantity: Quantity | null;
  updateInventoryQuantity: boolean | null;
};

/*
 * The listing of a singular material, of which a List Of Materials has many
 */
export class Material {
  invRec: InventoryRecord;
  usedQuantity: Quantity | null;
  updateInventoryQuantity: boolean | null;
  editing: boolean = false;
  originalUsedValue: number | null = null;
  originalUsedId: number | null = null;
  originalInventoryValue: number | null = null;
  originalInventoryId: number | null = null;
  usedQuantityDelta: number = 0;
  selected: boolean = false;

  constructor(
    attrs: MaterialAttrs & { updateInventoryQuantity?: boolean | null }
  ) {
    makeObservable(this, {
      invRec: observable,
      usedQuantity: observable,
      updateInventoryQuantity: observable,
      selected: observable,
      editing: observable,
      originalUsedValue: observable,
      originalUsedId: observable,
      originalInventoryValue: observable,
      originalInventoryId: observable,
      usedQuantityDelta: observable,
      updateQuantityEstimate: action,
      cancelChange: action,
      setEditing: action,
      setOriginalValues: action,
      updateDelta: action,
      setUsedQuantity: action,
      toggleSelected: action,
      toggleUpdateInventoryRecord: action,
      paramsForBackend: computed,
      enoughInventoryQuantity: computed,
      enoughLeft: computed,
      usedQuantityChanged: computed,
      usedQuantityLabel: computed,
      inventoryQuantityLabel: computed,
      canEditQuantity: computed,
    });
    this.invRec = attrs.invRec;
    this.usedQuantity = attrs.usedQuantity;
    this.updateInventoryQuantity = this.invRec instanceof SubSampleModel;
  }

  /*
   * Performs a deep copy of this, except for Result objects.
   */
  // eslint-disable-next-line no-use-before-define
  clone(): Material {
    void this.invRec.fetchAdditionalInfo();
    return new Material({
      invRec: this.invRec,
      usedQuantity: this.usedQuantity && { ...this.usedQuantity },
      updateInventoryQuantity: this.updateInventoryQuantity,
    });
  }

  isEqual(other: Material): boolean {
    return [
      this.invRec.globalId === other.invRec.globalId,
      this.usedQuantity?.unitId === other.usedQuantity?.unitId,
      this.usedQuantity?.numericValue === other.usedQuantity?.numericValue,
    ].every((x) => x);
  }

  get paramsForBackend(): MaterialBackendParams {
    return {
      invRec: { id: this.invRec.id, type: this.invRec.type },
      usedQuantity: this.usedQuantity,
      updateInventoryQuantity: this.updateInventoryQuantity,
    };
  }

  updateQuantityEstimate() {
    // based on checkbox, update store before/without saving, to display an immediate estimate
    if (this.invRec instanceof SubSampleModel && this.usedQuantity) {
      const r = this.invRec;
      if (this.originalInventoryId === null)
        throw new Error("originalInventoryId has not been initialised");
      const revertedDelta = fromCommonUnit(
        this.updateInventoryQuantity && !isNaN(this.usedQuantityDelta)
          ? this.usedQuantityDelta
          : 0,
        this.originalInventoryId
      );
      if (this.originalInventoryValue === null)
        throw new Error("originalInventoryValue has not been initialised");
      const revertedOriginalInventoryValue = fromCommonUnit(
        this.originalInventoryValue,
        this.originalInventoryId
      );
      if (r.quantity) {
        const q = r.quantity;
        q.numericValue = parseFloat(
          (revertedOriginalInventoryValue - revertedDelta).toFixed(3)
        );
      }
    }
  }

  cancelChange() {
    const usedQ = this.usedQuantity;
    if (usedQ) {
      if (this.originalUsedValue === null)
        throw new Error("originalUsedValue has not been initialised");
      if (this.originalUsedId === null)
        throw new Error("originalUsedId has not been initialised");
      usedQ.numericValue = fromCommonUnit(
        this.originalUsedValue,
        this.originalUsedId
      );
      usedQ.unitId = this.originalUsedId;
      this.usedQuantityDelta = 0;
      this.updateQuantityEstimate();
    }
  }

  setEditing(value: boolean, cancel: boolean = false) {
    if (value) {
      const usedQ = this.usedQuantity;
      const r = this.invRec;
      if (r instanceof SubSampleModel && usedQ) {
        this.setOriginalValues(
          usedQ.numericValue,
          usedQ.unitId,
          r.quantityValue,
          r.quantityUnitId
        );
      }
    } else if (cancel) this.cancelChange();
    this.editing = value;
  }

  setOriginalValues(
    usedValue: number,
    usedId: number,
    invValue: number,
    invId: number
  ) {
    this.originalUsedValue = toCommonUnit(usedValue, usedId);
    this.originalUsedId = usedId;
    this.originalInventoryValue = toCommonUnit(invValue, invId);
    this.originalInventoryId = invId;
  }

  updateDelta(additionalValue: number, id: number) {
    const newDelta = toCommonUnit(additionalValue, id);
    this.usedQuantityDelta = newDelta;
  }

  setUsedQuantity(additionalValue: number, unitId: number) {
    if (this.invRec instanceof SubSampleModel) {
      /* store difference (from original) */
      const newDelta = isNaN(additionalValue)
        ? 0
        : toCommonUnit(additionalValue, unitId);
      this.updateDelta(isNaN(additionalValue) ? 0 : additionalValue, unitId);

      /* update used quantity value */
      if (this.originalUsedValue === null)
        throw new Error("originalUsedValue has not been initialised");
      if (this.originalUsedId === null)
        throw new Error("originalUsedId has not been initialised");
      if (this.usedQuantity === null)
        throw new Error("usedQuantity has not been initialised");
      const newValue = this.originalUsedValue + newDelta;
      this.usedQuantity.numericValue = fromCommonUnit(
        newValue,
        this.originalUsedId
      );
    }
  }

  get enoughInventoryQuantity(): boolean {
    const r = this.invRec;
    if (r instanceof SubSampleModel) {
      const roundedDelta = parseFloat(this.usedQuantityDelta.toFixed(3)) ?? 0;
      if (this.originalInventoryValue === null)
        throw new Error("originalInventoryValue has not been initialised");
      const remainingAmount = this.originalInventoryValue;
      return roundedDelta <= remainingAmount;
    }
    // returns a nominal value as this value is only used when condition above is true
    return false;
  }

  get enoughLeft(): boolean {
    return this.editing && this.updateInventoryQuantity
      ? this.enoughInventoryQuantity
      : true;
  }

  toggleSelected(): void {
    this.selected = !this.selected;
  }

  toggleUpdateInventoryRecord() {
    this.updateInventoryQuantity = !this.updateInventoryQuantity;
  }

  get usedQuantityChanged(): boolean {
    // can be negative too
    return !isNaN(this.usedQuantityDelta) && this.usedQuantityDelta !== 0;
  }

  quantityUnitLabel(unitId: number | null): string {
    if (!unitId) return "?";
    const unitStore = getRootStore().unitStore;
    const unit = unitStore.getUnit(unitId);
    return unit?.label ?? "-";
  }

  get usedQuantityLabel(): string {
    return this.usedQuantity
      ? `${this.usedQuantity.numericValue} ${this.quantityUnitLabel(
          this.usedQuantity.unitId
        )}`
      : "";
  }

  get inventoryQuantityLabel(): string {
    return hasQuantity(this.invRec)
      .map((r) => {
        if (!r.quantity) return "";
        const { numericValue, unitId } = r.quantity;
        return `${numericValue} ${this.quantityUnitLabel(unitId)}`;
      })
      .orElse("");
  }

  get canEditQuantity(): boolean {
    return (
      this.invRec instanceof SubSampleModel &&
      this.invRec.canEdit &&
      !this.invRec.deleted
    );
  }
}

export type ListOfMaterialsAttrs = {
  id: number | null;
  name: string;
  description: string;
  elnFieldId: number;
  materials: Array<{
    invRec: ContainerAttrs | SampleAttrs | SubSampleAttrs;
    usedQuantity: Quantity | null;
  }>;
};

type ListOfMaterialsBackendParams = {
  name: string;
  description: string;
  elnFieldId: ElnFieldId;
  materials: Array<MaterialBackendParams>;
};

/*
 * A particular Listing of Materials, of which an ELN field may have many
 */
export class ListOfMaterials {
  id: ListOfMaterialsId;
  name: string;
  description: string;
  elnFieldId: ElnFieldId;
  materials: Array<Material>;
  loading: boolean = false;
  // @ts-expect-error Is definitely set in the constructor
  canEdit: boolean;
  editingMode: boolean = false;
  pickerSearch: Search;
  additionalQuantity: Quantity | null = null; // shared by all selected materials

  constructor(attrs: ListOfMaterialsAttrs) {
    makeObservable(this, {
      id: observable,
      name: observable,
      description: observable,
      elnFieldId: observable,
      materials: observable,
      loading: observable,
      canEdit: observable,
      pickerSearch: observable,
      editingMode: observable,
      additionalQuantity: observable,
      setEditingMode: action,
      setName: action,
      setDescription: action,
      setMetadata: action,
      addMaterials: action,
      removeMaterial: action,
      trackInventoryRecordsUpdate: action,
      setLoading: action,
      setAdditionalQuantity: action,
      paramsForBackend: computed,
      canEditQuantities: computed,
      isValid: computed,
      validAdditionalAmount: computed,
      enoughLeft: computed,
      selectedMaterials: computed,
      selectedCategories: computed,
      mixedSelectedCategories: computed,
    });
    this.id = attrs.id;
    this.name = attrs.name;
    this.description = attrs.description;
    this.elnFieldId = attrs.elnFieldId;
    const factory = new MemoisedFactory();
    this.materials = attrs.materials.map(
      (m) =>
        new Material({
          ...m,
          invRec: factory.newRecord(
            m.invRec as Record<string, unknown> & { globalId: GlobalId }
          ),
        })
    );
    this.setLoading(false);
    this.pickerSearch = new Search({
      uiConfig: {
        allowedTypeFilters: new Set([
          "SUBSAMPLE",
          "SAMPLE",
          "CONTAINER",
          "ALL",
        ]),
        selectionMode: "MULTIPLE",
      },
      factory: new MemoisedFactory(),
    });
    this.updatePickerSearch();

    const materialsStore = getRootStore().materialsStore;
    if (typeof materialsStore.canEdit === "boolean") {
      this.canEdit = materialsStore.canEdit;
    } else {
      void materialsStore.fetchCanEdit(this.id).then((canEdit) => {
        this.canEdit = canEdit;
      });
    }
  }

  setLoading(value: boolean): void {
    this.loading = value;
  }

  setEditingMode(value: boolean): void {
    this.editingMode = value;
    // reset additional quantity and cancel changes for each material
    if (!value) {
      this.setAdditionalQuantity(null);
      this.selectedMaterials.forEach((m) => {
        m.toggleSelected();
        m.setEditing(false, true);
      });
    }
  }

  updatePickerSearch() {
    this.pickerSearch.alwaysFilterOut = (result) =>
      new RsSet(this.materials)
        .map((mat) => mat.invRec.globalId)
        .has(result.globalId) || !result.usableInLoM;
  }

  /*
   * Performs a deep copy of this, except for Result objects.
   */
  // eslint-disable-next-line no-use-before-define
  clone(): ListOfMaterials {
    const copy = new ListOfMaterials({
      ...this,
      materials: [],
    });
    copy.materials = this.materials.map((m) => m.clone());
    return copy;
  }

  isEqual(other: ListOfMaterials): boolean {
    return [
      this.id === other.id,
      this.name === other.name,
      this.description === other.description,
      this.elnFieldId === other.elnFieldId,
      this.materials.length === other.materials.length &&
        ArrayUtils.zipWith(this.materials, other.materials, (x, y) =>
          x.isEqual(y)
        ).every((x) => x),
    ].every((x) => x);
  }

  setName(name: string) {
    this.name = name;
  }

  setDescription(description: string) {
    this.description = description;
  }

  setMetadata(name: string, description: string) {
    this.name = name;
    this.description = description;
  }

  setAdditionalQuantity(additionalQuantity: Quantity | null) {
    this.additionalQuantity = additionalQuantity;
  }

  addMaterials(records: Array<InventoryRecord>) {
    this.materials = [
      ...this.materials,
      ...records.map(
        (r) =>
          new Material({
            invRec: r,
            usedQuantity:
              r instanceof SubSampleModel && r.quantity !== null
                ? { unitId: r.quantity.unitId, numericValue: 0 }
                : null,
          })
      ),
    ];
    this.updatePickerSearch();
  }

  removeMaterial(material: Material): void {
    const i = this.materials.findIndex(
      (m) => m.invRec.globalId === material.invRec.globalId
    );
    if (i === -1)
      throw new Error("Provided material is not in the list of materials");
    this.materials.splice(i, 1);
    this.updatePickerSearch();
  }

  get paramsForBackend(): ListOfMaterialsBackendParams {
    const bParams: {
      materials: ReadonlyArray<Material>;
    } & ListOfMaterialsBackendParams & {
        id?: unknown;
        pickerSearch?: unknown;
      } = { ...this };
    delete bParams.id;
    delete bParams.pickerSearch;
    return {
      ...bParams,
      materials: bParams.materials.map((m) => m.paramsForBackend),
    };
  }

  get canEditQuantities(): boolean {
    return this.canEdit && Boolean(this.id) && this.materials.length > 0;
  }

  get isValid(): boolean {
    return (
      this.materials.length > 0 &&
      this.name.length <= 255 &&
      this.description.length <= 255
    );
  }

  get validAdditionalAmount(): boolean {
    if (this.additionalQuantity)
      return this.additionalQuantity.numericValue >= 0;
    return true;
  }

  get enoughLeft(): boolean {
    return this.selectedMaterials.every((m) => m.enoughLeft);
  }

  get selectedMaterials(): Array<Material> {
    return this.materials.filter((m) => m.selected);
  }

  get selectedCategories(): Set<UnitCategory> {
    return new RsSet(
      filterForThoseWithQuantities(this.selectedMaterials.map((m) => m.invRec))
    ).map((r) => r.quantityCategory);
  }

  get mixedSelectedCategories(): boolean {
    return this.selectedCategories.size > 1;
  }

  trackInventoryRecordsUpdate() {
    if (
      this.materials.some(
        (m) => m.updateInventoryQuantity && m.usedQuantityDelta
      )
    ) {
      getRootStore().trackingStore.trackEvent(
        "ListOfMaterialsInventoryQuantityEdited",
        {
          lomid: this.id,
          elnFieldId: this.elnFieldId,
        }
      );
    }
  }

  async create(): Promise<void> {
    this.setLoading(true);
    try {
      const { data } = await InvApiService.post<{ id: Id }>(
        `listOfMaterials`,
        this.paramsForBackend
      );
      this.id = data.id; // making list 'not new'
      getRootStore().uiStore.addAlert(
        mkAlert({
          message: `${this.name} was successfully created.`,
          variant: "success",
        })
      );
      this.trackInventoryRecordsUpdate();
      getRootStore().trackingStore.trackEvent("ListOfMaterialsCreated", {
        id: this.id,
        elnFieldId: this.elnFieldId,
      });
    } catch (error) {
      const data = Parsers.objectPath(["response", "data"], error)
        .flatMap(Parsers.isObject)
        .flatMap(Parsers.isNotNull);
      const errors = data
        .flatMap(Parsers.getValueWithKey("errors"))
        .flatMap(Parsers.isArray)
        .flatMap((e) => Result.any(...e.map(Parsers.isString)))
        .orElse([]);
      const message = data
        .flatMap(Parsers.getValueWithKey("message"))
        .orElseTry(() =>
          Parsers.isObject(error)
            .flatMap(Parsers.isNotNull)
            .flatMap((e) => Parsers.getValueWithKey("message")(e))
        )
        .flatMap(Parsers.isString)
        .orElse("Unknown reason.");
      getRootStore().uiStore.addAlert(
        mkAlert({
          title: `Something went wrong while updating ${this.name}`,
          message: errors.length > 0 ? "Expand for more information." : message,
          variant: "error",
          details: errors.map((e) => ({
            title: e,
            variant: "error",
          })),
        })
      );
      console.error(`Error creating new List of Materials`, error);
      throw error;
    } finally {
      this.setLoading(false);
    }
  }

  resetUsedQuantityChanges() {
    this.selectedMaterials.forEach((m) => {
      m.usedQuantityDelta = 0;
    });
  }

  async update(): Promise<void> {
    if (!this.id) throw new Error("A new list cannot be updated.");
    const id = this.id;
    this.setLoading(true);
    try {
      await InvApiService.update<void>(
        `listOfMaterials`,
        id,
        this.paramsForBackend
      );
      /* reset list additional quantity */
      this.setAdditionalQuantity(null);
      /* reset or hasListChanged fails */
      this.resetUsedQuantityChanges();
      /* reset materials selection */
      this.selectedMaterials.forEach((m) => m.toggleSelected());

      getRootStore().uiStore.addAlert(
        mkAlert({
          message: `${this.name} was successfully updated.`,
          variant: "success",
        })
      );
      this.trackInventoryRecordsUpdate();
      getRootStore().trackingStore.trackEvent("ListOfMaterialsUpdated", {
        id: this.id,
        elnFieldId: this.elnFieldId,
      });
    } catch (error) {
      const data = Parsers.objectPath(["response", "data"], error)
        .flatMap(Parsers.isObject)
        .flatMap(Parsers.isNotNull);
      const errors = data
        .flatMap(Parsers.getValueWithKey("errors"))
        .flatMap(Parsers.isArray)
        .flatMap((e) => Result.any(...e.map(Parsers.isString)))
        .orElse([]);
      const message = data
        .flatMap(Parsers.getValueWithKey("message"))
        .orElseTry(() =>
          Parsers.isObject(error)
            .flatMap(Parsers.isNotNull)
            .flatMap((e) => Parsers.getValueWithKey("message")(e))
        )
        .flatMap(Parsers.isString)
        .orElse("Unknown reason.");
      getRootStore().uiStore.addAlert(
        mkAlert({
          title: `Something went wrong while updating ${this.name}`,
          message: errors.length > 0 ? "Expand for more information." : message,
          variant: "error",
          details: errors.map((e) => ({
            title: e,
            variant: "error",
          })),
        })
      );
      console.error(`Error updating List of Materials ${this.id}`, error);
      Parsers.objectPath(["response", "status"], error)
        .flatMap(Parsers.isNumber)
        .do((status) => {
          if (status === 404) throw new Error("Resource could not be found.");
          if (status === 422) throw new Error("Query could not be processed.");
        });
      throw error;
    } finally {
      this.setLoading(false);
    }
  }

  async delete(): Promise<boolean> {
    const id = this.id;
    if (!id) throw new Error("A new list cannot be deleted.");

    const confirmation = await getRootStore().uiStore.confirm(
      "Are you sure you want to delete this list?",
      "This list and information about used quantities will not be available anymore. The inventory items will not be affected by this action.",
      "Yes",
      "No"
    );
    if (!confirmation) return confirmation;

    this.setLoading(true);
    try {
      await showToastWhilstPending(
        `Deleting ${this.name}...`,
        InvApiService.delete<void>(`listOfMaterials`, id)
      );
      getRootStore().uiStore.addAlert(
        mkAlert({
          message: `${this.name} was successfully deleted.`,
          variant: "success",
        })
      );
      getRootStore().trackingStore.trackEvent("ListOfMaterialsDeleted", {
        id,
        elnFieldId: this.elnFieldId,
      });
      return confirmation;
    } catch (error) {
      getRootStore().uiStore.addAlert(
        mkAlert({
          title: `Something went wrong while deleting ${this.name}`,
          message: getErrorMessage(error, "Unknown reason."),
          variant: "error",
        })
      );
      console.error(`Error deleting List of Materials ${this.id}`, error);
      throw error;
    } finally {
      this.setLoading(false);
    }
  }

  async export(exportOptions: ExportOptions): Promise<void> {
    const id = this.id;
    if (!id) throw new Error("A new list cannot be exported.");

    this.setLoading(true);
    try {
      const {
        exportMode,
        includeSubsamplesInSample,
        includeContainerContent,
        resultFileType,
      } = exportOptions;
      const globalIds = [`LM${id}`];
      const params = new FormData();
      params.append(
        "exportSettings",
        JSON.stringify({
          globalIds,
          exportMode,
          // if omitted, ZIP is assumed
          ...(resultFileType === null ? {} : { resultFileType }),
          // leaving values as string in RadioField, converting to boolean here
          ...(includeSubsamplesInSample === null
            ? {}
            : {
                includeSubsamplesInSample:
                  includeSubsamplesInSample === "INCLUDE",
              }),
          ...(includeContainerContent === null
            ? {}
            : {
                includeContainerContent: includeContainerContent === "INCLUDE",
              }),
        })
      );
      const { data } = await showToastWhilstPending(
        `Exporting ${this.name}...`,
        InvApiService.post<{ _links: Array<{ link: string; rel: string }> }>(
          "export",
          params
        )
      );
      const downloadLink = data._links[1];
      const fileName = downloadLink.link.split("downloadArchive/")[1];
      // create link for download
      const link = document.createElement("a");
      link.setAttribute("href", downloadLink.link);
      link.setAttribute("rel", downloadLink.rel);
      link.setAttribute("download", fileName);
      link.click(); // trigger download

      getRootStore().trackingStore.trackEvent("ListOfMaterialsExported", {
        id: this.id,
        elnFieldId: this.elnFieldId,
      });
    } catch (error) {
      const data = Parsers.objectPath(["response", "data"], error)
        .flatMap(Parsers.isObject)
        .flatMap(Parsers.isNotNull);
      const errors = data
        .flatMap(Parsers.getValueWithKey("errors"))
        .flatMap(Parsers.isArray)
        .flatMap((e) => Result.any(...e.map(Parsers.isString)))
        .orElse([]);
      const message = data
        .flatMap(Parsers.getValueWithKey("message"))
        .orElseTry(() =>
          Parsers.isObject(error)
            .flatMap(Parsers.isNotNull)
            .flatMap((e) => Parsers.getValueWithKey("message")(e))
        )
        .flatMap(Parsers.isString)
        .orElse("Unknown reason.");
      getRootStore().uiStore.addAlert(
        mkAlert({
          title: `Something went wrong while exporting ${this.name}`,
          message: errors.length > 0 ? "Expand for more information." : message,
          variant: "error",
          details: errors.map((e) => ({
            title: e,
            variant: "error",
          })),
        })
      );
      console.error(`Error exporting List of Materials ${this.id}`, error);
      Parsers.objectPath(["response", "status"], error)
        .flatMap(Parsers.isNumber)
        .do((status) => {
          if (status === 404) throw new Error("Resource could not be found.");
          if (status === 422) throw new Error("Query could not be processed.");
        });
      throw error;
    } finally {
      this.setLoading(false);
    }
  }

  async moveAllToBench(): Promise<void> {
    const { uiStore, moveStore, peopleStore, materialsStore } = getRootStore();
    if (!peopleStore.currentUser) throw new Error("Current user is not known.");
    const currentUser = peopleStore.currentUser;

    const allMaterials = new RsSet(
      filterForThoseWithLocations(this.materials.map((m) => m.invRec))
    );
    const parentIsBench = allMaterials.filter((r) =>
      r.isDirectlyOnWorkbenchOfUser(currentUser)
    );
    const parentIsOnBench = allMaterials
      .filter((r) => r.isOnWorkbenchOfUser(currentUser))
      .subtract(parentIsBench);

    let moving = allMaterials.subtract(parentIsBench).subtract(parentIsOnBench);
    if (
      !parentIsOnBench.isEmpty &&
      (await uiStore.confirm(
        "Some items are in containers that are already on your bench.",
        <>
          The items are:
          <ul>
            {parentIsOnBench.map(({ name, globalId }) => (
              <li key={globalId}>
                {name} ({globalId})
              </li>
            ))}
          </ul>
          Do you want to move them to your bench?
        </>,
        "Yes",
        "No"
      ))
    ) {
      moving = moving.union(parentIsOnBench);
    }
    if (moving.isEmpty) return;

    /*
     * calling getBench can be expensive and all the data that move needs is
     * known so this dummyBench object mocks the returned value from getBench
     */
    const dummyBench = new ContainerModel(new AlwaysNewFactory());
    dummyBench.id = currentUser.workbenchId;
    dummyBench.globalId = `BE${currentUser.workbenchId}`;
    dummyBench.cType = "WORKBENCH";

    moveStore.selectedResults = [...moving];
    await moveStore.setIsMoving(true);
    await moveStore.setTargetContainer(dummyBench);
    await moveStore.moveSelected();
    const updatedList = await materialsStore.getMaterialsListing(this.id);
    runInAction(() => {
      this.materials = updatedList.materials;
    });
  }
}
