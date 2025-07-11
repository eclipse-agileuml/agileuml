#include <iostream>
#include <ostream>
#include <vector>

using std::vector;
using std::cout;
using std::endl;

class MatrixLib {
    public:
        MatrixLib() {};
        static vector<double> rowMult(vector<double> s, vector<vector<double>> m) {
            vector<double> result;
            for (size_t i = 0; i < s.size(); i++) {
                double sum = 0.0;
                for (size_t k = 0; k < m.size(); k++) {
                    sum += s[k] * m[k][i];
                }
                result.push_back(sum);
            }
            return result;
        };
        static vector<vector<double>> rowMultiplication(vector<vector<double>> m1, vector<vector<double>> m2) {
            vector<vector<double>> result;
            for (vector<double> row : m1) {
                result.push_back(rowMult(row, m2));
            }
            return result;
        };
};

int main() {
    vector<vector<double>> list = MatrixLib::rowMultiplication({{1.0, 2.0}, {3.0, 4.0}}, {{5.0, 6.0}, {7.0, 8.0}});
    // cout << list.size() << endl;
    for (auto value : list) {
        for (auto innervalue : value) {
            cout << innervalue << ", ";
        }
        cout << endl;
    }
    cout << "end" << endl;
    return 0;
}