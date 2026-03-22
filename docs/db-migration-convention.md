# Team migration convention

## Яагаад Flyway ашиглах вэ?
`spring.jpa.hibernate.ddl-auto=update` нь production орчинд schema-г Hibernate-ээр шууд өөрчлөх эрсдэлтэй.
Иймээс schema өөрчлөлт бүрийг version-тэй SQL migration байдлаар хадгалж, dev/test/prod орчинд ижил дарааллаар ажиллуулна.
Мөн одоо байгаа орчинд Flyway-г аюулгүй нэвтрүүлэхийн тулд dev/prod profile дээр `baseline-on-migrate=true` тохируулсан.

## Стандарт
- Migration файлууд `src/main/resources/db/migration` дотор байрлана.
- Нэршилт: `V<version>__<snake_case_description>.sql`
- Нэг migration = нэг зорилго.
- Аль хэдийн production-д гарсан migration-ийг хэзээ ч rewrite хийж болохгүй.
- Schema өөрчлөлтөөс гадна шаардлагатай index, constraint-уудаа тусад нь migration болгоно.

## Санал болгож буй урсгал
1. Entity/model өөрчлөхөөсөө өмнө schema хэрэгцээгээ тодорхойлно.
2. SQL migration файл нэмнэ.
3. Local дээр migration ажиллаж байгааг шалгана.
4. Repository/service тестүүдээр шинэ schema дүрэм эвдээгүйг баталгаажуулна.
5. PR дээр migration-ийн зорилго, rollback/impact-ийг тайлбарлана.

## Versioning дүрэм
- `V1` нь initial schema.
- Дараагийн өөрчлөлтүүд `V2`, `V3`, ... дарааллаар өснө.
- Хэрэв нэг feature олон өөрчлөлттэй бол index/constraint-ийг тусад нь салгаж бичнэ.

## Review checklist
- Production-д backward-compatible юу?
- Existing data-г эвдэх эрсдэлтэй юу?
- Index шаардлагатай query-нуудыг хамарсан уу?
- Constraint нь service-level validation-тай зөрчилдөхгүй юу?
