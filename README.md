# Regen

Regen is a library that has reflection and generics utilities. The most notable one is a generic specification that converts a model called Condition (that can be received as JSON from client-side) to a Predicate that is used to search data in your relational database. It can be used with Spring Framework or any other project capable of using spring's specification.

## Installation

```bash
mvn clean install
```
Get the jar file generated in the target folder and import it in your project.

## Usage
To use regen's generic specification you can instantiate a ConditionFacade and call any of it's methods passing a condition instance:
```java
    ConditionFacade<YourModel> facade = new ConditionFacade<YourModel>();
    List<YourModel> yourList = facade.findAllByCondition(
        new Condition(), yourModelRepository, YourModel.class
    );
```
ConditionFacade has three basic methods: 
```java
    public Page<T> findAllByCondition(
        Condition condition, Pageable pageable, JpaSpecificationExecutor<T> executor, Class<T> clazz
    );
    public List<T> findAllByCondition(Condition condition, JpaSpecificationExecutor<T> executor, Class<T> clazz);
    public Optional<T> findFirstByCondition(Condition condition, JpaSpecificationExecutor<T> executor, Class<T> clazz);
```
If for some reason you need to use regen's generic specification in a different way you can do it like this:
```java
    // Instantiate a generic specification passing your condition and the class of the model it should return
    Specification<T> specification = new GenericSpecification<T>(condition, clazz);
    // Pass the specification instance to your repository
    repository.findAll(specification);
```
The Condition class has two basic properties:
```java
    Condition condition = new Condition();
    condition.setField(/*The field of your class as string*/);
    condition.setValue(/*the value of that field*/);
```
You can also just pass these arguments in the constructor:
```java
    Condition condition = new Condition("myField", "myValue");
```
Or you can receive it as JSON from client-side:
```json
    {
        "field": "myField",
        "value": "myValue"
    }
```
**WARNING**
In order to work properly your composite key classes need to implement equals

For a detailed step by step tutorial on how to use regen please check the wiki: https://github.com/teorema-community/regen/wiki

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.
