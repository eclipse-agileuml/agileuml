#include <iostream>
#include <string>
#include <vector>
#include <typeinfo>

using std::string;
using std::vector;
using std::cout;
using std::endl;

template <typename T>
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
            for (vector<double> row: m1) {
                result.push_back(rowMult(row, m2));
            }
            return result;
        };
        static vector<T> subRows(vector<T> m, vector<int> s) {
            vector<T> result;
            for (auto integer :s) {
                if ((integer >= 0) && (integer <= m.size() - 1)) {
                    result.push_back(m[integer]);
                }
            }
            return result;
        }
        static vector<T> subMatrix(vector<vector<T>> m, vector<int> rows, vector<int> cols) {
            vector<T> result;
            for (auto row: rows) {
                if ((row >= 0) && (row <= m.size() - 1)) {
                    result.push_back(subRows(m[row], cols));
                }
            }
            return result;
        }
        static vector<vector<T>> matrixExcludingRowColumn(vector<T> m, int row, int col) {
            vector<vector<T>> result;
            for (int i = 0; i < m.size(); i++) {
                if (i == row) {
                    vector<T> r = m[i];
                    for (int j = 0; j < r.size(); i++) {
                        if (j != col) {
                            vector<T> subrow = r[j];
                            result.push_back(subrow);
                        }
                    }
                }
            }
            return result;
        }
        // static vector<T> column(vector<T> m, int i) {
        //     if (string(typeid(m[i]).name()).find("vector") != string::npos) {
        //         return {m[i]};
        //     }
        //     return m[i];
        // }
        static vector<int> shape(T* x) {
            vector<int> result = {0};
            if (string(typeid(x).name()).find("vector") != string::npos) {
                vector<T> sq = {&x};
                result = {sq.size()};
                if (sq.size() > 0) {
                    for (auto v : shape(sq[0])) {
                        if (find(sq[0].begin(), sq[0].end(), v) == v.end()) {
                            result.push_back(v);
                        }
                    }
                }
            }
            return result;
        }
};

int main() {
    // MatrixLib<int>::column({1, 2, 3, 4}, 0);
    // MatrixLib<vector<int>>::column({{1, 2}, {3, 4}}, 0);
    // vector<vector<double>> list = MatrixLib<void>::rowMultiplication({{1.0, 2.0}, {3.0, 4.0}}, {{5.0, 6.0}, {7.0, 8.0}});
    vector<double> param = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0,8.0};
    vector<double>* param_pointer = &param;
    vector<int> list = MatrixLib<vector<double>>::shape(param_pointer);
    // cout << list.size() << endl;
    for (auto value : list) {
        cout << value << endl;
        // for (auto innervalue : value) {
        //     cout << innervalue << ", ";
        // }
        // cout << endl;
    }
    cout << "end" << endl;
    return 0;
}