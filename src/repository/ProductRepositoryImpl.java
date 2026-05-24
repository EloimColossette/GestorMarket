package repository;
import java.util.logging.Logger;
import java.util.logging.Level;
import database.Database;
import model.ProductModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepositoryImpl implements ProductRepository {

    private static final Logger logger =
            Logger.getLogger(
                    ProductRepositoryImpl.class.getName()
            );

    @Override
    public void save(ProductModel productModel){
        String sql = """
                        INSERT INTO products (
                            name
                        )
                        VALUES (?)
                    """;
        try(Connection conn = Database.conectar();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ){
            stmt.setString(
                    1,
                    productModel.getName()
            );
            stmt.executeUpdate();

            logger.info(
                    "Product saved successfully!"
            );
        }catch (SQLException e){
            logger.log(
                    Level.SEVERE,
                    "Error saving product",
                    e
            );
        };
    }

    @Override
    public List<ProductModel> findAll(){
        List<ProductModel> productModels = new ArrayList<>();

        String sql = """
                        SELECT * FROM products
                    """;

        try(Connection conn = Database.conectar();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet resultSet = stmt.executeQuery()
        ){
            while (resultSet.next()){
                ProductModel productModel = new ProductModel();

                productModel.setProductsId(
                        resultSet.getInt(
                                "products_id"
                        )
                );

                productModel.setName(
                        resultSet.getString(
                                "name"
                        )
                );
            }
        }catch (SQLException e ){
            logger.log(
                    Level.SEVERE,
                    "Error loading products",
                    e
            );
        }
        return productModels;
    }
}
