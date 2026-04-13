#include <stdio.h>
#include <jansson.h>
#include "../ex4/psc_tp2.h"

// URL do produto a ser consultado
#define PRODUCT_URI "https://dummyjson.com/products/1"

// Definições das chaves do JSON que serão extraídas
#define ID_KEY "id"
#define TITLE_KEY "title"
#define DESCRIPTION_KEY "description"
#define CATEGORY_KEY "category"
#define PRICE_KEY "price"
#define DISCOUNT_PERCENTAGE_KEY "discountPercentage"
#define RATING_KEY "rating"
#define STOCK_KEY "stock"
#define TAGS_KEY "tags"
#define BRAND_KEY "brand"
#define SKU_KEY "sku"
#define WEIGHT_KEY "weight"
#define DIMENSIONS_KEY "dimensions"
#define DIMENSIONS_WIDTH_KEY "width"
#define DIMENSIONS_HEIGHT_KEY "height"
#define DIMENSIONS_DEPTH_KEY "depth"
#define WARRANTY_INFORMATION_KEY "warrantyInformation"
#define SHIPPING_INFORMATION_KEY "shippingInformation"
#define AVAILABILITY_STATUS_KEY "availabilityStatus"
#define RETURN_POLICY_KEY "returnPolicy"
#define MINIMUM_ORDER_QUANTITY_KEY "minimumOrderQuantity"
#define META_KEY "meta"
#define META_CREATED_AT_KEY "createdAt"
#define META_UPDATED_AT_KEY "updatedAt"
#define META_BARCODE_KEY "barcode"
#define META_QR_CODE_KEY "qrCode"
#define IMAGES_KEY "images"
#define THUMBNAIL_KEY "thumbnail"

// Função para imprimir um array de strings do JSON
void print_string_array(json_t *array, const char *array_name) {
    if (json_is_array(array)) {
        printf("\t%s:\n", array_name); // Nome do array
        size_t index;
        json_t *element;
        json_array_foreach(array, index, element) { // Itera pelos elementos do array
            const char *value = json_string_value(element);
            if (value) {
                printf("\t\t%s\n", value); // Imprime cada valor do array
            } else {
                printf("\t\t(invalid value)\n"); // Caso o valor seja inválido
            }
        }
    } else {
        printf("\t%s: (not an array or missing)\n", array_name); // Caso não seja um array
    }
}

int main(int argc, char *argv[]) {
    // Obtém o JSON a partir da URL
    json_t *json = http_get_json(PRODUCT_URI);
    if (!json) {
        printf("Erro ao obter o JSON\n");
        return 1;
    }

    // Declaração das variáveis para armazenar os dados do JSON
    int r, id, stock, weight, minimumOrderQuantity;
    char *title, *description, *category, *brand, *sku, *warrantyInformation, *shippingInformation, *availabilityStatus;
    char *returnPolicy, *createAt, *updateAt, *barcode, *qrCode, *thumbnail;
    float price, discountPercentage, rating, width, height, depth;
    json_t *tags, *images;

    // Extração de valores do JSON usando json_unpack
    r = json_unpack(json,
        "{s:i, s:s, s:s, s:s, s:i, s:o, s:s, s:s, s:i, s:s, s:s, s:s, s:s, s:i, s:{s:s, s:s, s:s, s:s}, s:o, s:s}",
        ID_KEY, &id,
        TITLE_KEY, &title,
        DESCRIPTION_KEY, &description,
        CATEGORY_KEY, &category,
        STOCK_KEY, &stock,
        TAGS_KEY, &tags,
        BRAND_KEY, &brand,
        SKU_KEY, &sku,
        WEIGHT_KEY, &weight,
        WARRANTY_INFORMATION_KEY, &warrantyInformation,
        SHIPPING_INFORMATION_KEY, &shippingInformation,
        AVAILABILITY_STATUS_KEY, &availabilityStatus,
        RETURN_POLICY_KEY, &returnPolicy,
        MINIMUM_ORDER_QUANTITY_KEY, &minimumOrderQuantity,
        META_KEY,
        META_CREATED_AT_KEY, &createAt,
        META_UPDATED_AT_KEY, &updateAt,
        META_BARCODE_KEY, &barcode,
        META_QR_CODE_KEY, &qrCode,
        IMAGES_KEY, &images,
        THUMBNAIL_KEY, &thumbnail);

    // Extrai valores numéricos adicionais diretamente do JSON
    price = json_number_value(json_object_get(json, PRICE_KEY));
    discountPercentage = json_number_value(json_object_get(json, DISCOUNT_PERCENTAGE_KEY));
    rating = json_number_value(json_object_get(json, RATING_KEY));

    // Extrai valores relacionados às dimensões do produto
    json_t *dimensions = json_object_get(json, DIMENSIONS_KEY);
    if (dimensions) {
        width = json_number_value(json_object_get(dimensions, DIMENSIONS_WIDTH_KEY));
        height = json_number_value(json_object_get(dimensions, DIMENSIONS_HEIGHT_KEY));
        depth = json_number_value(json_object_get(dimensions, DIMENSIONS_DEPTH_KEY));
    }

    // Verifica se houve erro ao processar o JSON
    if (r != 0) {
        printf("Erro ao interpretar o JSON\n");
        return 1;
    }

    // Imprime os valores extraídos
    printf("\tID: %d\n", id);
    printf("\tTitle: %s\n", title);
    printf("\tDescription: %s\n", description);
    printf("\tCategory: %s\n", category);
    printf("\tPrice: %.2f\n", price);
    printf("\tDiscountPercentage: %.2f\n", discountPercentage);
    printf("\tRating: %.2f\n", rating);
    printf("\tStock: %d\n", stock);
    print_string_array(tags, "Tags");
    printf("\tBrand: %s\n", brand);
    printf("\tSku: %s\n", sku);
    printf("\tWeight: %d\n", weight);
    printf("\tDimensions:\n");
    printf("\t\tWidth: %.2f\n", width);
    printf("\t\tHeight: %.2f\n", height);
    printf("\t\tDepth: %.2f\n", depth);
    printf("\tWarranty Information: %s\n", warrantyInformation);
    printf("\tShipping Information: %s\n", shippingInformation);
    printf("\tAvailability Status: %s\n", availabilityStatus);
    printf("\tReturn Policy: %s\n", returnPolicy);
    printf("\tMinimum Order Quantity: %d\n", minimumOrderQuantity);
    printf("\tMeta:\n");
    printf("\t\tCreated At: %s\n", createAt);
    printf("\t\tUpdated At: %s\n", updateAt);
    printf("\t\tBarcode: %s\n", barcode);
    printf("\t\tQR Code: %s\n", qrCode);
    print_string_array(images, "Images");
    printf("\tThumbnail: %s\n", thumbnail);

    // Liberta a memória utilizada pelo JSON
    json_decref(json);

    return 0;
}
/*

Definição de Chaves:

As macros #define facilitam a definição das chaves do JSON, permitindo que sejam reutilizadas no código.
Função print_string_array:

Lê e imprime arrays de strings presentes no JSON, útil para listas como "tags" ou "imagens".
Função json_unpack:

Extrai os dados do JSON usando um formato de mapeamento {s:t}, onde s é a chave e t é o tipo de dados.
Estrutura Geral do main:

Lê o JSON de uma API remota usando http_get_json.
Extrai os valores esperados usando json_unpack e funções auxiliares.
Imprime os valores extraídos no formato apropriado.
Tratamento de Dimensões:

As dimensões são processadas separadamente, pois estão aninhadas dentro de um objeto JSON.
Limpeza de Recursos:

O JSON é libertado com json_decref, prevenindo fugas de memória.

*/
