# Regen

Regen is a library that has reflection and generics utilities. The most notable one is a generic specification that converts a model called Condition (that can be received as JSON from client-side) to a Predicate that is used to search data in your relational database. It can be used with Spring Framework or any other project capable of using spring's specification.

## Installation

```bash
mvn clean install
```
Get the jar file generated in the target folder and import it in your project.

## Usage
To use regen's generic specification you can instantiate a ConditionMediator and call any of it's methods passing a condition instance:
```java
    ConditionMediator<YourModel> mediator = new ConditionMediator<YourModel>();
    List<YourModel> yourList = mediator.findAllByCondition(
        new Condition(), yourModelRepository, YourModel.class
    );
```
ConditionMediator has three basic methods: 
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

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.