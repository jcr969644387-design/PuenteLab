-- PuenteLab — sample_data.sql
-- Muestra representativa de los datos semilla reales que inserta DatabaseSeeder.kt en el primer
-- arranque (Kotlin es la fuente de verdad: ver app/src/main/java/.../data/seed/). Este archivo
-- es una referencia legible en SQL, no un script que la app ejecute.

INSERT INTO materials (id, name, description, strength, costPerUnit, weightFactor, allowedRoles, colorHex, iconKey, unlockLevel) VALUES
('wood', 'Madera de Obra', 'Barata y fácil de cortar. Ideal para empezar, pero se dobla en tramos largos.', 40.0, 3.0, 0.15, 'DECK,BRACE', '#B4783C', 'material_wood', 1),
('rope', 'Cuerda Reforzada', 'Muy barata y liviana. Solo aguanta tracción.', 20.0, 2.0, 0.05, 'CABLE,BRACE', '#D8C08A', 'material_rope', 1),
('stone', 'Piedra Tallada', 'Pesada y resistente a compresión. Perfecta para arcos.', 100.0, 5.0, 0.5, 'DECK,BRACE,TOWER', '#8C8C82', 'material_stone', 1),
('steel', 'Acero Estructural', 'El material más versátil.', 90.0, 6.0, 0.25, 'DECK,BRACE,TOWER', '#5C7A99', 'material_steel', 1),
('steel_cable', 'Cable de Acero', 'Cable de alta resistencia para puentes colgantes.', 70.0, 8.0, 0.10, 'CABLE', '#3E5266', 'material_cable', 2),
('concrete', 'Hormigón Armado', 'Muy resistente pero pesado.', 110.0, 9.0, 0.40, 'DECK,BRACE,TOWER', '#9AA0A6', 'material_concrete', 3),
('aluminum', 'Aluminio Aeronáutico', 'Ligero y resistente.', 65.0, 7.0, 0.12, 'DECK,BRACE,TOWER', '#C7CDD6', 'material_aluminum', 4),
('carbon_fiber', 'Fibra de Carbono', 'Material de recompensa: altísima resistencia, muy poco peso.', 130.0, 14.0, 0.08, 'DECK,BRACE,CABLE,TOWER', '#2B2E33', 'material_carbon', 6);

INSERT INTO vehicles (id, name, description, iconKey, themeScenario, weightMultiplier, unlockLevel) VALUES
('van_explorer', 'Carreta de Exploración', 'Ligera y ágil, perfecta para las primeras pruebas.', 'vehicle_cart', 'RIVER', 1.0, 1),
('buggy', 'Buggy Todoterreno', 'Ruedas grandes para cruzar cañones irregulares.', 'vehicle_buggy', 'CANYON', 1.1, 1),
('forest_truck', 'Camión Forestal', 'Transporta troncos: pesa más de lo que parece.', 'vehicle_truck', 'FOREST', 1.3, 1),
('tram', 'Tranvía Urbano', 'Lleva pasajeros por la ciudad elevada.', 'vehicle_tram', 'CITY', 1.4, 1),
('expedition_truck', 'Camión de Expedición', 'Equipo pesado de montaña.', 'vehicle_expedition', 'MOUNTAIN', 1.5, 1),
('pivot_hauler', 'Transporte de PIVOT', 'El vehículo especial del estudio.', 'vehicle_pivot', 'RIVER', 1.8, 5);

-- Ejemplo de 3 de los 45 desafíos (el primero, uno intermedio y el último de RIVER).
-- fixedSupports usa el formato "x:y;x:y" (ver Converters.kt).
INSERT INTO bridge_challenges (id, scenario, orderIndex, name, spanUnits, leftBankX, leftBankY, rightBankX, rightBankY, fixedSupports, budget, demand, maxSlope, budgetMarginFor2Stars, budgetMarginFor3Stars, maxStressFor3Stars, recommendedStructure, narrativeIntro, narrativeSuccess) VALUES
('river_01', 'RIVER', 1, 'Río Correntoso · Nivel 1', 4.0, 0.0, 0.0, 4.0, 0.0, '1.333333:0.0;2.666667:0.0', 130.0, 'LOW', 0.8, 0.10, 0.25, 0.75, 'BEAM', 'El río creció esta semana y la balsa de siempre ya no alcanza. Diseña un cruce firme.', '¡El vehículo cruzó el río sin mojarse las ruedas!'),
('river_05', 'RIVER', 5, 'Río Correntoso · Nivel 5', 8.8, 0.0, 0.0, 8.8, 0.0, '2.933333:-1.14;5.866667:-1.14', 165.0, 'MEDIUM', 0.6875, 0.10, 0.25, 0.75, 'TRUSS', 'La corriente es fuerte en este tramo. PIVOT sugiere reforzar bien los apoyos.', 'Cruce exitoso. El agua sigue corriendo abajo, tranquila.'),
('river_09', 'RIVER', 9, 'Río Correntoso · Nivel 9', 13.6, 0.0, 0.0, 13.6, 0.0, '4.533333:-1.86;9.066667:-1.86', 225.0, 'HIGH', 0.55, 0.10, 0.25, 0.75, 'SUSPENSION', 'Un grupo de excursionistas espera al otro lado. Necesitan un puente confiable, no uno bonito.', 'PIVOT registra otro cruce limpio. El río ya no separa nada.');

INSERT INTO badges (id, name, description, iconKey) VALUES
('PRIMER_PUENTE', 'Primer Puente', 'Completa tu primer desafío.', 'badge_primer_puente'),
('EXPLORADOR', 'Explorador/a', 'Completa al menos un desafío en cada escenario.', 'badge_explorador'),
('VETERANO', 'Veterano/a del Estudio', 'Aprueba 25 desafíos en total.', 'badge_veterano');
-- (9 insignias en total; ver SeedBadgesAndStamps.kt para la lista completa)
