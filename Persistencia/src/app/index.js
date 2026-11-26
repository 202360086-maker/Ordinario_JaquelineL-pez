import { useEffect, useState } from 'react';
import {
  Alert,
  Button, FlatList,
  SafeAreaView,
  StyleSheet,
  Text, TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { supabase } from '../supabase';

export default function App() {
  const [productos, setProductos] = useState([]);
  const [producto, setProducto] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [precio, setPrecio] = useState('');
  const [editandoId, setEditandoId] = useState(null);

  // c) Prueba de conexión + e) READ
  const cargarProductos = async () => {
    const { data, error } = await supabase
      .from('productos')
      .select('*')
      .order('id', { ascending: true });
    if (error) {
      Alert.alert('Error de conexión', error.message);
      return;
    }
    setProductos(data);
  };

  useEffect(() => {
    cargarProductos();
  }, []);

  const limpiarForm = () => {
    setProducto('');
    setDescripcion('');
    setPrecio('');
    setEditandoId(null);
  };

  // d) CREATE  y  f) UPDATE
  const guardar = async () => {
    if (!producto.trim() || !precio.trim()) {
      Alert.alert('Faltan datos', 'Producto y precio son obligatorios');
      return;
    }
    const datos = { producto, descripcion, precio: parseFloat(precio) };

    const { error } = editandoId
      ? await supabase.from('productos').update(datos).eq('id', editandoId)
      : await supabase.from('productos').insert([datos]);

    if (error) {
      Alert.alert('Error', error.message);
      return;
    }
    limpiarForm();
    cargarProductos();
  };

  const editar = (item) => {
    setEditandoId(item.id);
    setProducto(item.producto);
    setDescripcion(item.descripcion ?? '');
    setPrecio(String(item.precio));
  };

  // g) DELETE
  const eliminar = (id) => {
    Alert.alert('Eliminar', '¿Seguro que quieres eliminar este registro?', [
      { text: 'Cancelar', style: 'cancel' },
      {
        text: 'Eliminar',
        style: 'destructive',
        onPress: async () => {
          const { error } = await supabase.from('productos').delete().eq('id', id);
          if (error) Alert.alert('Error', error.message);
          else cargarProductos();
        },
      },
    ]);
  };

  return (
    <SafeAreaView style={styles.container}>
      <Text style={styles.titulo}>Productos</Text>

      <TextInput style={styles.input} placeholder="Producto" value={producto} onChangeText={setProducto} />
      <TextInput style={styles.input} placeholder="Descripción" value={descripcion} onChangeText={setDescripcion} />
      <TextInput
        style={styles.input}
        placeholder="Precio"
        value={precio}
        onChangeText={setPrecio}
        keyboardType="decimal-pad"
      />

      <View style={styles.fila}>
        <Button title={editandoId ? 'Actualizar' : 'Agregar'} onPress={guardar} />
        {editandoId && <Button title="Cancelar" color="gray" onPress={limpiarForm} />}
      </View>

      <FlatList
        data={productos}
        keyExtractor={(item) => String(item.id)}
        style={{ marginTop: 16 }}
        ListEmptyComponent={<Text style={styles.vacio}>Sin registros</Text>}
        renderItem={({ item }) => (
          <View style={styles.card}>
            <View style={{ flex: 1 }}>
              <Text style={styles.nombre}>{item.producto}</Text>
              <Text>{item.descripcion}</Text>
              <Text style={styles.precio}>${item.precio}</Text>
            </View>
            <TouchableOpacity onPress={() => editar(item)}>
              <Text style={styles.editar}>Editar</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => eliminar(item.id)}>
              <Text style={styles.eliminar}>Borrar</Text>
            </TouchableOpacity>
          </View>
        )}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16, paddingTop: 48, backgroundColor: '#fff' },
  titulo: { fontSize: 24, fontWeight: 'bold', marginBottom: 12 },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 6, padding: 8, marginBottom: 8 },
  fila: { flexDirection: 'row', justifyContent: 'space-around' },
  card: {
    flexDirection: 'row', alignItems: 'center', gap: 12,
    padding: 12, marginBottom: 8, borderRadius: 8, backgroundColor: '#f2f2f2',
  },
  nombre: { fontWeight: 'bold', fontSize: 16 },
  precio: { color: 'green', fontWeight: '600' },
  editar: { color: '#0066cc' },
  eliminar: { color: 'crimson' },
  vacio: { textAlign: 'center', color: 'gray', marginTop: 20 },
});
