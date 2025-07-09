#include <iostream>
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
};

int main() {
    vector<double> list = MatrixLib::rowMult({2.0, 2.0}, {{4.0, 6.0}});
    cout << list.size() << endl;
    for (auto value : list) {
        cout << value << ", ";
    }
    cout << "end" << endl;
    return 0;
}